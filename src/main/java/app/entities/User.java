package app.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String name;

    @Column
    private String email;

    @Column
    private int age;

    @Column
    private double height;

    @Column
    private double weight;

    @Enumerated(EnumType.STRING)
    @Column
    private ExperienceLevel experienceLevel;

    @Column
    private int trainingDaysPerWeek;

    @Enumerated(EnumType.STRING)
    @Column
    private TrainingGoal goal;

    public User(String name, String email, int age, double height, double weight, ExperienceLevel experienceLevel, int trainingDaysPerWeek, TrainingGoal goal) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.experienceLevel = experienceLevel;
        this.trainingDaysPerWeek = trainingDaysPerWeek;
        this.goal = goal;
    }



}
