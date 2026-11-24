import { useEffect, useState } from 'react'
import './WorkoutProgram.css'

function WorkoutProgram({ userId, token, workoutProgramId }) {
  const [program, setProgram] = useState(null)
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')

  useEffect(() => {
    if (!workoutProgramId) {
      return
    }

    async function getWorkoutProgram() {
      const response = await fetch(`/api/users/${userId}/workout-program`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })

      if (!response.ok) {
        setMessage('Kunne ikke hente workout programmet')
        return
      }

      const data = await response.json()
      setProgram(data)
    }

    getWorkoutProgram()
  }, [userId, token, workoutProgramId])

  async function generateWorkoutProgram() {
    setLoading(true)
    setMessage('')

    const response = await fetch(`/api/users/${userId}/workout-program`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })

    if (!response.ok) {
      setMessage('Kunne ikke generere workout programmet')
      setLoading(false)
      return
    }

    const data = await response.json()

    setProgram(data)
    setLoading(false)
  }

  return (
    <section className="workout-panel">
      <h2>Workout program</h2>

      <button
        type="button"
        onClick={generateWorkoutProgram}
        disabled={loading}
      >
        {loading
          ? 'Genererer...'
          : program
            ? 'Generér nyt workout program'
            : 'Generér workout program'}
      </button>

      {message && <p>{message}</p>}

      {program && (
        <div className="workout-program">
          <h3>{program.name}</h3>

          <p>{program.description}</p>

          <p>Træningsdage om ugen: {program.trainingDaysPerWeek}</p>

          <h3>Exercises</h3>

          {program.exercises.map((exercise) => (
            <div className="exercise-card" key={exercise.id}>
              <h4>{exercise.name}</h4>
              <p>Muskelgruppe: {exercise.muscleGroup}</p>
              <p>Sets: {exercise.sets}</p>
              <p>Reps: {exercise.reps}</p>
            </div>
          ))}
        </div>
      )}
    </section>
  )
}

export default WorkoutProgram