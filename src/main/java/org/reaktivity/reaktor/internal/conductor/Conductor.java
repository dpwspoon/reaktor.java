/**
 * Copyright 2016-2017 The Reaktivity Project
 *
 * The Reaktivity Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.reaktivity.reaktor.internal.conductor;

import static java.nio.ByteBuffer.allocateDirect;

import java.util.function.IntFunction;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.MessageHandler;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.broadcast.BroadcastTransmitter;
import org.agrona.concurrent.ringbuffer.RingBuffer;
import org.reaktivity.nukleus.Nukleus;
import org.reaktivity.nukleus.function.CommandHandler;
import org.reaktivity.reaktor.internal.Context;
import org.reaktivity.reaktor.internal.acceptor.Acceptor;
import org.reaktivity.reaktor.internal.types.control.ErrorFW;
import org.reaktivity.reaktor.internal.types.control.FrameFW;
import org.reaktivity.reaktor.internal.types.control.RouteFW;
import org.reaktivity.reaktor.internal.types.control.RoutedFW;
import org.reaktivity.reaktor.internal.types.control.UnrouteFW;
import org.reaktivity.reaktor.internal.types.control.UnroutedFW;

public final class Conductor implements Nukleus
{
    private final FrameFW frameRO = new FrameFW();
    private final RouteFW routeRO = new RouteFW();
    private final UnrouteFW unrouteRO = new UnrouteFW();

    private final ErrorFW.Builder errorRW = new ErrorFW.Builder();
    private final RoutedFW.Builder routedRW = new RoutedFW.Builder();
    private final UnroutedFW.Builder unroutedRW = new UnroutedFW.Builder();

    private final RingBuffer conductorCommands;
    private final BroadcastTransmitter conductorResponses;
    private final MutableDirectBuffer sendBuffer;
    private final MessageHandler commandHandler;

    private Acceptor acceptor;
    private IntFunction<CommandHandler> commandHandlerSupplier;

    public Conductor(
        Context context)
    {
        this.conductorCommands = context.conductorCommands();
        this.conductorResponses = context.conductorResponses();
        this.sendBuffer = new UnsafeBuffer(allocateDirect(context.maxControlResponseLength()));
        this.commandHandler = this::handleCommand;
    }

    public void setAcceptor(
        Acceptor acceptor)
    {
        this.acceptor = acceptor;
    }

    public void onError(
        long correlationId)
    {
        ErrorFW error = errorRW.wrap(sendBuffer, 0, sendBuffer.capacity())
                .correlationId(correlationId)
                .build();

        conductorResponses.transmit(error.typeId(), error.buffer(), error.offset(), error.sizeof());
    }

    public void onRouted(
        long correlationId,
        long sourceRef)
    {
        RoutedFW routed = routedRW.wrap(sendBuffer, 0, sendBuffer.capacity())
                .correlationId(correlationId)
                .sourceRef(sourceRef)
                .build();

        conductorResponses.transmit(routed.typeId(), routed.buffer(), routed.offset(), routed.sizeof());
    }

    public void onUnrouted(
        long correlationId)
    {
        UnroutedFW unrouted = unroutedRW.wrap(sendBuffer, 0, sendBuffer.capacity())
                .correlationId(correlationId)
                .build();

        conductorResponses.transmit(unrouted.typeId(), unrouted.buffer(), unrouted.offset(), unrouted.sizeof());
    }

    @Override
    public int process()
    {
        return conductorCommands.read(commandHandler);
    }

    public void setCommandHandlerSupplier(
        IntFunction<CommandHandler> commandHandlerSupplier)
    {
        this.commandHandlerSupplier = commandHandlerSupplier;
    }

    @Override
    public String name()
    {
        return "conductor";
    }

    private void handleCommand(
        int msgTypeId,
        DirectBuffer buffer,
        int index,
        int length)
    {
        switch (msgTypeId)
        {
        case RouteFW.TYPE_ID:
            final RouteFW route = routeRO.wrap(buffer, index, index + length);
            acceptor.doRoute(route);
            break;
        case UnrouteFW.TYPE_ID:
            final UnrouteFW unroute = unrouteRO.wrap(buffer, index, index + length);
            acceptor.doUnroute(unroute);
            break;
        default:
            handleUnrecognized(msgTypeId, buffer, index, length);
            break;
        }
    }

    private void handleUnrecognized(
        int msgTypeId,
        DirectBuffer buffer,
        int index,
        int length)
    {
        CommandHandler handler = commandHandlerSupplier.apply(msgTypeId);
        if (handler != null)
        {
            handler.handle(buffer, index, length, conductorResponses::transmit, sendBuffer);
        }
        else
        {
            final FrameFW frame = frameRO.wrap(buffer, index, index + length);
            onError(frame.correlationId());
        }
    }
}
