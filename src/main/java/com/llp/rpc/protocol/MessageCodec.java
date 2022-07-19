package com.llp.rpc.protocol;

import com.llp.rpc.config.Config;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;
import com.llp.rpc.message.Message;

import java.util.List;
@Slf4j
@ChannelHandler.Sharable
public class MessageCodec extends MessageToMessageCodec<ByteBuf, Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        ByteBuf buf = ctx.alloc().buffer();
        // 1. 4 字节的魔数
        buf.writeBytes(new byte[]{1,2,3,4});
        // 2. 1 字节的版本
        buf.writeByte(1);
        // 3. 1 字节的序列化方式 jdk 0 , json 1
        buf.writeByte(Config.getSerializerAlgorithm().ordinal());
        // 4. 1 字节的指令类型
        buf.writeByte(msg.getMessageType());
        // 5. 消息的序列号，4 个字节
        buf.writeInt(msg.getSequenceId());
        // 无意义，对齐填充
        buf.writeByte(0xff);
        // 6. 获取消息对象的字节数组
        byte[] bytes = Config.getSerializerAlgorithm().serialize(msg);
        // 7. 长度
        buf.writeInt(bytes.length);
        // 8. 写入内容
        buf.writeBytes(bytes);
        out.add(buf);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        int magicNum = msg.readInt();
        byte version = msg.readByte();
        byte serializerAlgorithm = msg.readByte(); // 0 或 1
        byte messageType = msg.readByte(); // 0,1,2...
        int sequenceId = msg.readInt();
        msg.readByte();
        int length = msg.readInt();
        byte[] bytes = new byte[length];
        msg.readBytes(bytes, 0, length);

        // 找到反序列化算法
        Algorithm algorithm = Algorithm.values()[serializerAlgorithm];

        // 确定具体消息类型
        Class<? extends Message> messageClass = Message.getMessageClass(messageType);
        Message message = algorithm.deserialize(messageClass, bytes);
        //log.debug("{}, {}, {}, {}, {}, {}", magicNum, version, serializerAlgorithm, messageType, sequenceId, length);
        //log.debug("{}", com.llp.rpc.annotation.message);
        out.add(message);
    }
}
