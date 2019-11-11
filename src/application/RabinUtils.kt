package application

import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception
import java.math.BigInteger
import java.util.*

val ZERO = 0.toBigInteger()
val ONE = 1.toBigInteger()
val TWO = 2.toBigInteger()
val THREE = 3.toBigInteger()
val FOUR = 4.toBigInteger()

fun encode(M: BigInteger, N: BigInteger, B: BigInteger): BigInteger {
    return M * (M + B) % N
}

fun decode(C: BigInteger, P: BigInteger, Q: BigInteger, B: BigInteger): List<Int> {
    val N = P * Q
    val D = (B * B + FOUR * C) % N
    val Mp = D.modPow((P + ONE) / FOUR, P) //S
    val Mq = D.modPow((Q + ONE) / FOUR, Q) //R
    val EXT_GSD = gcdWide(P, Q)
    val Yp = EXT_GSD[1]
    val Yq = EXT_GSD[2]

    val d1 = (Yp * P * Mq + Yq * Q * Mp) % N
    val d2 = N - d1
    val d3 = (Yp * P * Mq - Yq * Q * Mp) % N
    val d4 = N - d3
    val d = listOf(d1, d2, d3, d4)

    val m = mutableListOf<Int>()
    for (di in d) {
        if ((di - B) % TWO == ZERO) {
            m.add((((-B + di) / TWO) % N).toInt())
        } else {
            m.add((((-B + di + N) / TWO) % N).toInt())
        }
    }
    return m
}

fun gcdWide(a: BigInteger, b: BigInteger): List<BigInteger> {
    if (b === BigInteger.ZERO) {
        return listOf(a, BigInteger.ONE, BigInteger.ZERO)
    }

    val temp = gcdWide(b, a.mod(b))

    return listOf(temp[0], temp[2], temp[1].subtract(a.divide(b).multiply(temp[2])))
}

fun fileEncrypt(fileName: String, N: BigInteger, B: BigInteger) {
    val encryptedValues = mutableListOf<BigInteger>()
    val inStream = FileInputStream(fileName)
    val bytes = inStream.readAllBytes()
    inStream.close()
    for (byte in bytes) {
        val M = byte.toInt() + 128
        encryptedValues.add(encode(M.toBigInteger(), N, B))
    }
    val outStream = BufferedOutputStream(FileOutputStream(fileName) as OutputStream)
    for (i in encryptedValues) {
        outStream.write(i.toBytes(N.bitLength()))
    }
    outStream.close()
}

fun fileDecrypt(fileName: String, P: BigInteger, Q: BigInteger, B: BigInteger) {
    val N = P * Q
    val blockSize = getBlockSize(N.bitLength())
    val inStream = FileInputStream(fileName)
    val bytes = inStream.readAllBytes()
    inStream.close()
    val blockCount = bytes.size / blockSize
    var k = 0

    val resBytes = mutableListOf<Byte>()

    for (i in 0 until blockCount) {
        val block = ByteArray(blockSize)
        for (j in 0 until blockSize) {
            block[j] = bytes[k]
            k++
        }
        val C = BigInteger(block)
        val MM = decode(C, P, Q, B)
        for (s in MM) {
            if (s <= 255) {
                resBytes.add((s - 128).toByte())
            }
        }
    }
    val outStream = FileOutputStream(fileName)
    outStream.write(resBytes.toByteArray())
    outStream.close()
}

fun checkPrivateKey(key: BigInteger): Boolean{
    return key % FOUR == THREE
}

fun checkValue(P: String, Q: String, B: String): Boolean{
    if (P.isBlank() || Q.isBlank() || B.isBlank()){
        return false
    }

    try {
        val P = BigInteger(P)
        val Q = BigInteger(Q)
        val N = P * Q
        return checkValue(N.toString(), P.toString(), Q.toString(), B.toString())
    }catch (e: Exception){
        return false
    }
}

fun checkValue(N: String, P: String, Q: String, B: String): Boolean{

    if (N.isBlank() || P.isBlank() || Q.isBlank() || B.isBlank()){
        return false
    }
    try {
        val N = BigInteger(N)
        val P = BigInteger(P)
        val Q = BigInteger(Q)
        val B = BigInteger(B)

        if (B >= N) {
            return false
        }
        if (N != P * Q) {
            return false
        }
        if ((!checkPrivateKey(P)) || (!checkPrivateKey(Q))) {
            return false
        }
        return true
    }catch (e: Exception){
        return false
    }
}

fun generateB(N: BigInteger): BigInteger {
    val length = N.bitLength() - 1
    return BigInteger(length, 1, java.util.Random(System.currentTimeMillis()))
}