package com.llp.rpc.server.handler;


import com.llp.rpc.common.message.RpcRequestMessage;
import com.llp.rpc.common.message.RpcResponseMessage;
import com.llp.rpc.server.factory.ServiceFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * Rpc请求处理器
 *
 * @author llp
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {

    /**
     * 读事件
     * @param ctx
     * @param message
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage message) {

        RpcResponseMessage responseMessage=new RpcResponseMessage();
        //设置请求的序号
        responseMessage.setSequenceId(message.getSequenceId());
        Object result;
        try {
            //通过名称从工厂获取本地注解了@RpcServer的实例
            Object service = ServiceFactory.serviceFactory.get(message.getInterfaceName());
            //获取方法     方法名，参数
            Method method = service.getClass().getMethod(message.getMethodName(),message.getParameterTypes());
            //调用
            result = method.invoke(service, message.getParameterValue());
            //设置返回值
            responseMessage.setReturnValue(result);
        } catch (Exception e) {
            e.printStackTrace();
            responseMessage.setExceptionValue(new Exception("远程调用出错:"+e.getMessage()));
        }finally {
            ctx.writeAndFlush(responseMessage);
            //ReferenceCountUtil.release()其实是ByteBuf.release()方法
            //ReferenceCountUtil.release(com.llp.rpc.annotation.message);
        }
    }

}
