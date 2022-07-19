package com.llp.rpc.client.handler;


import com.llp.rpc.common.message.PingMessage;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 客户端发送心跳包
 *
 * @author llp
 */
@Slf4j
public class HeartBeatClientHandler extends ChannelDuplexHandler {

    /**
     * idlStatus写事件
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            IdleState state = event.state();
            //长时间没有写入数据  发送心跳包
            if (state == IdleState.WRITER_IDLE) {
                //获取ip
                log.debug("发送心跳包 {}", ctx.channel().remoteAddress());
                PingMessage message = new PingMessage();
                message.setSequenceId(0);
                ctx.writeAndFlush(message);
            }

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.debug("远程调用出错");
        cause.printStackTrace();
        ctx.close();
        super.exceptionCaught(ctx, cause);
    }


    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelUnregistered");
        ctx.close();
        super.channelUnregistered(ctx);
    }

}
