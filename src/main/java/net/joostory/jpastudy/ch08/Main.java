package net.joostory.jpastudy.ch08;

import javax.persistence.EntityManager;
import net.joostory.jpastudy.Entity_managerKt;

public class Main {
  private EntityManager em;

  public Main(EntityManager em) {
    this.em = em;
  }

  private void saveData() {
    Member2 member = new Member2();
    member.setUsername("회원1");
    em.persist(member);

    Team2 team = new Team2();
    team.setName("팀1");
    em.persist(team);
  }

  public void run() {
    saveData();
    em.flush();
    em.clear();
    referenceTeam();
  }

  private void referenceTeam() {
    Member2 member = em.find(Member2.class, 1L);
    Team2 team = em.getReference(Team2.class, 2L);
    System.out.println("team=" + team.getId());
    System.out.println("team=" + em.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(team));
    System.out.println("team=" + team.getClass().getName());
    member.setTeam(team);
  }


  public static void main(String argv[]) {
    System.out.println("Chapter08 (Java)");
    Entity_managerKt.runWithEntityManager(em -> {
      Main m = new Main(em);
      m.run();
      return null;
    });
  }
}
