package com.llp.rpc.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class ProtocolFrameDecoder extends LengthFieldBasedFrameDecoder {
    /*
    maxFrameLength 数据最大长度
    表示数据的最大长度（包括附加信息、长度标识等内容）

    lengthFieldOffset 数据长度标识的起始偏移量
    用于指明数据第几个字节开始是用于标识有用字节长度的，因为前面可能还有其他附加信息

    lengthFieldLength 数据长度标识所占字节数（用于指明有用数据的长度）
    数据中用于表示有用数据长度的标识所占的字节数

    lengthAdjustment 长度表示与有用数据的偏移量
    用于指明数据长度标识和有用数据之间的距离，因为两者之间还可能有附加信息

    initialBytesToStrip 数据读取起点
    读取起点，不读取 0 ~ initialBytesToStrip 之间的数据

 */
    public ProtocolFrameDecoder() {
        this(1024, 12, 4, 0, 0);
    }

    public ProtocolFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }
}
