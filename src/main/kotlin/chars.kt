class CharacterIterator(private val chars: CharArray) {
    private var current = 0

    private constructor(chars: CharArray, current: Int) : this(chars) {
        require(current < chars.size)
        this.current = current
    }

    fun hasNext(): Boolean {
        return current + 1 < chars.size
    }

    fun next(): CharacterIterator? {
        return if (hasNext()) CharacterIterator(chars, current + 1) else null
    }

    val char : Char? get() = if (current >= chars.size) null else chars[current]
}