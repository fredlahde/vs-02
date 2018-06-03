import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.*

object Main {

    /**
     * Die Anzahl der zu verteilenden Aufgaben
     */
    private const val MAX_OBJECTS = 10000

    /**
     * Die anzahl der [Node] auf die die Aufgaben verteilt werden
     */
    private const val NODES = 5

    @Throws(NoSuchAlgorithmException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        println("Starting initialisation...")
        println("generating 5 nodes")

        val nodeList = ArrayList<Node>()
        for (i in 1..NODES)
            nodeList.add(Node())


        println("deploying 10'000 Strings to nodes")
        val rs = RandomString()
        val md = MessageDigest.getInstance("MD5")
        for (i in 1..MAX_OBJECTS) {
            val bytes = rs.nextString().toByteArray(charset = Charset.forName("UTF-8"))
            val hashSum = md.digest(bytes).sum()
            val mod = Math.abs(hashSum % (nodeList.size))
            nodeList[mod].add()
        }
        

        println("Distribution")
        nodeList.forEachIndexed { index, node ->
            println("Node$index: ~${(node.get().toDouble() / MAX_OBJECTS * 100).toInt()}% -> ${node.get()}")
        }
    }
}

/**
 * Diese Klasse stellt einen Node dar, auf den die Aufgaben verteilt werden
 */
class Node {
    private var i = 0

    /**
     * fügt eine Aufgabe hinzu
     */
    fun add() = i++

    /**
     * gibt die Anzahl der auf den Node verteilten Aufgaben zurück
     *
     * @return die Aufgaben des Nodes
     */
    fun get(): Int = i
}

/**
 * Diese Klasse generiert zufällig Strings
 *
 * @param length Die Länge des generierten Strings
 * @param random Der [Random], der verwendet werden soll
 * @param symbols Der Symbolsatz, der verwendet werden soll
 */
class RandomString constructor(length: Int = 21,
                               private val random: Random = SecureRandom(),
                               symbols: String = alphaNum) {
    private val symbols: CharArray
    private val buf: CharArray

    /**
     * Generate a random string.
     * @return a random string with [buf] chars
     */
    fun nextString(): String = buf.indices.map { symbols[random.nextInt(symbols.size)] }.toString()

    init {
        if (length < 1) throw IllegalArgumentException()
        if (symbols.length < 2) throw IllegalArgumentException()
        this.symbols = symbols.toCharArray()
        this.buf = CharArray(length)
    }

    companion object {
        private const val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private val lower = upper.toLowerCase(Locale.ROOT)
        private const val digits = "0123456789"
        private val alphaNum = upper + lower + digits
    }
}

