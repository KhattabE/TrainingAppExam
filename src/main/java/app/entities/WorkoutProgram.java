package app.entities;


import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
public class WorkoutProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private int trainingDaysPerWeek;

    @ManyToMany
    @JoinTable(name = "workoutprogram_exercise", joinColumns = @JoinColumn(name = "workoutprogram_id"), inverseJoinColumns = @JoinColumn(name = "exercises_id"))
    private Set<Exercise> exercises = new LinkedHashSet<>();

    public WorkoutProgram(String name, String description, int trainingDaysPerWeek) {
        this.name = name;
        this.description = description;
        this.trainingDaysPerWeek = trainingDaysPerWeek;
    }


}
