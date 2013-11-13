/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
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

package io.netty.channel;

import java.net.SocketAddress;

import static io.netty.channel.DefaultChannelPipeline.*;

/**
 * A set of helper methods for easier implementation of custom {@link EventLoop} implementation.
 */
public final class EventLoopUtil {

    public static void invokeChannelRegisteredNow(ChannelHandlerContext ctx) {
        try {
            ((ChannelInboundHandler) ctx.handler()).channelRegistered(ctx);
        } catch (Throwable t) {
            notifyHandlerException(ctx, t);
        }
    }

    public static void invokeChannelActiveNow(final ChannelHandlerContext ctx) {
        try {
            ((ChannelInboundHandler) ctx.handler()).channelActive(ctx);
        } catch (Throwable t) {
            notifyHandlerException(ctx, t);
        }
    }

    public static void invokeChannelInactiveNow(final ChannelHandlerContext ctx) {
        try {
            ((ChannelInboundHandler) ctx.handler()).channelInactive(ctx);
        } catch (Throwable t) {
            notifyHandlerException(ctx, t);
        }
    }

    public static void invokeExceptionCaughtNow(final ChannelHandlerContext ctx, final Throwable cause) {
        try {
            ctx.handler().exceptionCaught(ctx, cause);
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn(
                        "An exception was thrown by a user handler's " +
                                "exceptionCaught() method while handling the following exception:", cause);
            }
        }
    }

    public static void invokeUserEventTriggeredNow(final ChannelHandlerContext ctx, final Object event) {
        try {
            ((ChannelInboundHandler) ctx.handler()).userEventTriggered(ctx, event);
        } catch (Throwable t) {
            notifyHandlerException(ctx, t);
        }
    }

    public static void invokeChannelReadNow(final ChannelHandlerContext ctx, final Object msg) {
        try {
            ((ChannelInboundHandler) ctx.handler()).channelRead(ctx, msg);
        } catch (Throwable t) {
            notifyHandlerException(ctx, t);
        }
    }

    public static void invokeChannelReadCompleteNow(final ChannelHandlerContext ctx) {
        try {
            ((ChannelInboundHandler) ctx.handler()).channelReadComplete(ctx);
        } catch (Throwable t) {
            notifyHandlerException(ctx, t);
        }
    }

    public static void invokeChannelWritabilityChangedNow(final ChannelHandlerContext ctx) {
        try {
            ((ChannelInboundHandler) ctx.handler()).channelWritabilityChanged(ctx);
        } catch (Throwable t) {
            notifyHandlerException(ctx, t);
        }
    }

    public static void invokeBindNow(
            final ChannelHandlerContext ctx, final SocketAddress localAddress, final ChannelPromise promise) {
        try {
            ((ChannelOutboundHandler) ctx.handler()).bind(ctx, localAddress, promise);
        } catch (Throwable t) {
            notifyOutboundHandlerException(t, promise);
        }
    }
    public static void invokeConnectNow(
            final ChannelHandlerContext ctx,
            final SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise promise) {
        try {
            ((ChannelOutboundHandler) ctx.handler()).connect(ctx, remoteAddress, localAddress, promise);
        } catch (Throwable t) {
            notifyOutboundHandlerException(t, promise);
        }
    }

    public static void invokeDisconnectNow(final ChannelHandlerContext ctx, final ChannelPromise promise) {
        try {
            ((ChannelOutboundHandler) ctx.handler()).disconnect(ctx, promise);
        } catch (Throwable t) {
            notifyOutboundHandlerException(t, promise);
        }
    }

    public static void invokeCloseNow(final ChannelHandlerContext ctx, final ChannelPromise promise) {
        try {
            ((ChannelOutboundHandler) ctx.handler()).close(ctx, promise);
        } catch (Throwable t) {
            notifyOutboundHandlerException(t, promise);
        }
    }

    public static void invokeReadNow(final ChannelHandlerContext ctx) {
        try {
            ((ChannelOutboundHandler) ctx.handler()).read(ctx);
        } catch (Throwable t) {
            notifyHandlerException(ctx, t);
        }
    }

    public static void invokeWriteNow(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        try {
            ((ChannelOutboundHandler) ctx.handler()).write(ctx, msg, promise);
        } catch (Throwable t) {
            notifyOutboundHandlerException(t, promise);
        }
    }

    public static void invokeFlushNow(final ChannelHandlerContext ctx) {
        try {
            ((ChannelOutboundHandler) ctx.handler()).flush(ctx);
        } catch (Throwable t) {
            notifyHandlerException(ctx, t);
        }
    }

    public static void invokeWriteAndFlushNow(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        invokeWriteNow(ctx, msg, promise);
        invokeFlushNow(ctx);
    }

    private static void notifyHandlerException(ChannelHandlerContext ctx, Throwable cause) {
        if (inExceptionCaught(cause)) {
            if (logger.isWarnEnabled()) {
                logger.warn(
                        "An exception was thrown by a user handler " +
                                "while handling an exceptionCaught event", cause);
            }
            return;
        }

        invokeExceptionCaughtNow(ctx, cause);
    }

    private static void notifyOutboundHandlerException(Throwable cause, ChannelPromise promise) {
        // only try to fail the promise if its not a VoidChannelPromise, as
        // the VoidChannelPromise would also fire the cause through the pipeline
        if (promise instanceof VoidChannelPromise) {
            return;
        }

        if (!promise.tryFailure(cause)) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to fail the promise because it's done already: {}", promise, cause);
            }
        }
    }

    private static boolean inExceptionCaught(Throwable cause) {
        do {
            StackTraceElement[] trace = cause.getStackTrace();
            if (trace != null) {
                for (StackTraceElement t : trace) {
                    if (t == null) {
                        break;
                    }
                    if ("exceptionCaught".equals(t.getMethodName())) {
                        return true;
                    }
                }
            }

            cause = cause.getCause();
        } while (cause != null);

        return false;
    }

    private EventLoopUtil() { }
}