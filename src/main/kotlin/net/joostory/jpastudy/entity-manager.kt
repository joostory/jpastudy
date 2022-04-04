package net.joostory.jpastudy

import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence

private val emf: EntityManagerFactory = Persistence.createEntityManagerFactory("jpastudy")

fun runWithEntityManager(logic: (em: EntityManager) -> Unit) {
    val em = emf.createEntityManager()
    val tx = em.transaction
    try {
        log("begin transaction")
        tx.begin()

        logic(em)

        log("commit transaction")
        tx.commit()
    } catch (e: Exception) {
        log("rollback ${e}")
        e.printStackTrace()
        tx.rollback()
    } finally {
        log("em.close")
        em.close()
    }
    log("emf.close")
    emf.close()
}
