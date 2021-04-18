package application

import java.math.BigInteger

operator fun BigInteger.rem(other: BigInteger): BigInteger = this.mod(other)

fun Byte.toBigInteger(): BigInteger = this.toInt().toBigInteger()

fun getBlockSize(bitCount: Int): Int{
    var byteCount = bitCount / 8
    return if (byteCount * 8 < bitCount) byteCount + 2 else byteCount + 1
}

fun BigInteger.toBytes(bitCount: Int): ByteArray{
    val blockSize = getBlockSize(bitCount)
    val bytes = this.toByteArray()
    val blockBytes = ByteArray(blockSize)

    val byteShift = blockSize - bytes.size

    for (i in bytes.indices){
        blockBytes[i + byteShift] = bytes[i]
    }
    return blockBytes
}


fun List<BigInteger>.toString() = run {
    for (i in this) {
        println(i.toString())
    }
}