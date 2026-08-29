package app.entities;


import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name;

    @Column
    @Enumerated(EnumType.STRING)
    private MuscleGroup muscleGroup;

    @Column
    private int sets;

    @Column
    private int reps;

    public Exercise(String name, MuscleGroup muscleGroup, int sets, int reps) {
        this.name = name;
        this.muscleGroup = muscleGroup;
        this.sets = sets;
        this.reps = reps;
    }
}
