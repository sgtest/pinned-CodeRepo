package com.crazymakercircle.netty.decoder;

/**
 * create by 尼恩 @ 疯狂创客圈
 **/

import com.crazymakercircle.util.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class Byte2IntegerDecoder extends ByteToMessageDecoder {

    //钩子实现
    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        while (in.readableBytes() >= 4) {
            Integer anInt = in.readInt();
            Logger.info("解码出一个整数: " + anInt);
            out.add(anInt);
        }
    }
}