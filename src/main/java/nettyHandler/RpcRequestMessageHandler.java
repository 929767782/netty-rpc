package nettyHandler;

import factory.ServiceFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import message.RpcRequestMessage;
import message.RpcResponseMessage;

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
            //通过名称从工厂获取本地实例
            Object service = ServiceFactory.getService(Class.forName(message.getInterfaceName()));
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
            //ReferenceCountUtil.release(message);
        }
    }

//    /**
//     * 读空闲
//     * @param ctx
//     * @param evt
//     * @throws Exception
//     */
//    @Override
//    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//        if (evt instanceof IdleStateEvent) {
//            IdleState state = ((IdleStateEvent) evt).state();
//            if (state == IdleState.READER_IDLE) {
//                log.info("长时间未收到心跳包，断开连接...");
//                ctx.close();
//            }
//        } else {
//            super.userEventTriggered(ctx, evt);
//        }
//    }
}
