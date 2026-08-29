package app.entities;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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
    private List<Exercise> exercises = new ArrayList<>();

    public WorkoutProgram(String name, String description, int trainingDaysPerWeek) {
        this.name = name;
        this.description = description;
        this.trainingDaysPerWeek = trainingDaysPerWeek;
    }


}
