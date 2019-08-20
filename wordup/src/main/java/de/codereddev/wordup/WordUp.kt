package de.codereddev.wordup

object WordUp {
    lateinit var config: WordUpConfig
        private set

    fun init(config: WordUpConfig) {
        this.config = config
    }

    fun initializeDatabase() {

    }
}
