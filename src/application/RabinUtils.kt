package application

import com.sun.javafx.scene.control.behavior.TwoLevelFocusBehavior
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.math.BigInteger

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
    // возведение в степень по модулю
    val Mp = D.modPow((P + ONE) / FOUR, P) //S
    val Mq = D.modPow((Q + ONE) / FOUR, Q) //R

    // расширенный алгоритм Евклида
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

fun fileEncrypt(fileName: String, N: BigInteger, B: BigInteger, input: StringBuilder, outOut: StringBuilder) {
    val encryptedValues = mutableListOf<BigInteger>()
    val inStream = FileInputStream(fileName)
    val bytes = inStream.readBytes()
    inStream.close()
    for (i in bytes.indices) {
        val M = bytes[i].toInt()
        input.append(M.toString())
        input.append("\n")
        val k = encode(M.toBigInteger(), N, B)
        encryptedValues.add(k)
    }
    val f = fileName
    val outStream = BufferedOutputStream(FileOutputStream(f) as OutputStream)
    for (i in encryptedValues.indices) {
        if (i < 20) {
            outOut.append(encryptedValues[i].toString())
            outOut.append("\n")
        }
        outStream.write(encryptedValues[i].toBytes(N.bitLength()))
    }
    outStream.close()
}

fun fileDecrypt(fileName: String, P: BigInteger, Q: BigInteger, B: BigInteger, input: StringBuilder, outOut: StringBuilder) {



    val N = P * Q
    val blockSize = getBlockSize(N.bitLength())
    val inStream = FileInputStream(fileName)
    val bytes = inStream.readBytes()
    inStream.close()
    for (i in bytes.indices) {
        val M = bytes[i].toInt()
        input.append(M.toString())
        input.append("\n")

    }
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
                resBytes.add((s).toByte())
                outOut.append(s)
                outOut.append("\n")
            }
        }
    }
    val outStream = FileOutputStream(fileName)
    outStream.write(resBytes.toByteArray())
    outStream.close()
}

fun checkPrivateKey(key: BigInteger): Boolean {

    return key % FOUR == THREE
}

//fun checkValue(P: String, Q: String, N: String, B: String): Boolean {
//    if (P.isBlank() || Q.isBlank() || B.isBlank()) {
//        return false
//    }
//
//    try {
//        val P = BigInteger(P)
//        val Q = BigInteger(Q)
//        val N = P * Q
//        return checkValue(N.toString(), P.toString(), Q.toString(), B.toString())
//    } catch (e: Exception) {
//        return false
//    }
//}

fun checkValue(N: String, P: String, Q: String, B: String): Int {

    if (N.isBlank() || P.isBlank() || Q.isBlank() || B.isBlank()) {
        return 1
    }
    try {
        val N = BigInteger(N)
        val P = BigInteger(P)
        val Q = BigInteger(Q)
        val B = BigInteger(B)

        if (B >= N) {
            return 2
        }

        if (N != P * Q) {
            return 3
        }

        if (!checkPrivateKey(P)) {
            return 4
        }

        if (!checkPrivateKey(Q)) {
            return 5
        }

        return 0
    } catch (e: Exception) {
        return 9
    }
}
//
//fun checkValue(P: String, Q: String): Int {
//    val P = BigInteger(P)
//    val Q = BigInteger(Q)
//    if ((!checkPrivateKey(P)) || (!checkPrivateKey(Q))) {
//        return 1
//    }
//    //todo простые
//    return 0
//
//}

fun generateB(N: BigInteger): BigInteger {
    val length = N.bitLength() - 1
    return BigInteger(length, 1, java.util.Random(System.currentTimeMillis()))
}

fun millerRabinTest(n: BigInteger, k: Int): Boolean {
    // если n == 2 или n == 3 - эти числа простые, возвращаем true
    if (n.compareTo(TWO) == 0 || n.compareTo(THREE) == 0) return true

    // если n < 2 или n четное - возвращаем false
    if (n < TWO || n.mod(TWO) == ZERO) return false

    // представим n − 1 в виде (2^s)·t, где t нечётно, это можно сделать последовательным делением n - 1 на 2
    var t = n.subtract(ONE);
    var s = 0
    while (t.mod(TWO) == ZERO) {
        t = t.divide(TWO)
        s += 1
    }

    // повторить k раз
    for (i in 0..k) {

        val a = generateB(n);

        // x ← a^t mod n, вычислим с помощью возведения в степень по модулю
        var x: BigInteger = a.modPow(t, n)

        // если x == 1 или x == n − 1, то перейти на следующую итерацию цикла
        if (x == ONE || x == n.subtract(ONE)) continue

        // повторить s − 1 раз
        for (r in 1 until s) {
            // x ← x^2 mod n
            x = x.modPow(TWO, n)

            // если x == 1, то вернуть "составное"
            if (x == ONE) return false

            // если x == n − 1, то перейти на следующую итерацию внешнего цикла
            if (x == n.subtract(ONE)) break
        }
        if (x != n.subtract(ONE)) return false
    }
    // вернуть "вероятно простое"
    return true
}
