package net.joostory.jpastudy.ch08;

import javax.persistence.*;

@Entity(name = "ch08Member2")
public class Member2 {
  @Id @GeneratedValue
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "NAME")
  private String username;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "team_id")
  private Team2 team;

  public Team2 getTeam() {
    return team;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setTeam(Team2 team) {
    this.team = team;
  }
}
