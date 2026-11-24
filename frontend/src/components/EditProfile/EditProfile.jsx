import { useState } from 'react'
import './EditProfile.css'

function EditProfile({ user, token, onUpdated, onCancel }) {
  const [name, setName] = useState(user.name)
  const [age, setAge] = useState(user.age)
  const [height, setHeight] = useState(user.height)
  const [weight, setWeight] = useState(user.weight)
  const [experienceLevel, setExperienceLevel] = useState(user.experienceLevel)
  const [trainingDaysPerWeek, setTrainingDaysPerWeek] = useState(
    user.trainingDaysPerWeek,
  )
  const [goal, setGoal] = useState(user.goal)
  const [message, setMessage] = useState('')

  async function handleSubmit(event) {
    event.preventDefault()

    const response = await fetch('/api/users/me', {
      method: 'PUT',
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        name,
        age: Number(age),
        height: Number(height),
        weight: Number(weight),
        experienceLevel,
        trainingDaysPerWeek: Number(trainingDaysPerWeek),
        goal,
      }),
    })

    if (!response.ok) {
      setMessage('Kunne ikke opdatere profilen')
      return
    }

    const updatedUser = await response.json()
    onUpdated(updatedUser)
  }

  return (
    <section className="edit-profile">
      <h2>Rediger profil</h2>

      <form onSubmit={handleSubmit}>
        <label className="edit-field">
          Navn
          <input
            type="text"
            value={name}
            onChange={(event) => setName(event.target.value)}
            required
          />
        </label>

        <label className="edit-field">
          Alder
          <input
            type="number"
            min="1"
            value={age}
            onChange={(event) => setAge(event.target.value)}
            required
          />
        </label>

        <label className="edit-field">
          Højde
          <input
            type="number"
            min="0.1"
            step="0.1"
            value={height}
            onChange={(event) => setHeight(event.target.value)}
            required
          />
        </label>

        <label className="edit-field">
          Vægt
          <input
            type="number"
            min="0.1"
            step="0.1"
            value={weight}
            onChange={(event) => setWeight(event.target.value)}
            required
          />
        </label>

        <label className="edit-field">
          Erfaringsniveau
          <select
            value={experienceLevel}
            onChange={(event) => setExperienceLevel(event.target.value)}
            required
          >
            <option value="BEGINNER">Begynder</option>
            <option value="INTERMEDIATE">Let øvet</option>
            <option value="ADVANCED">Øvet</option>
          </select>
        </label>

        <label className="edit-field">
          Træningsdage om ugen
          <input
            type="number"
            min="1"
            max="7"
            value={trainingDaysPerWeek}
            onChange={(event) =>
              setTrainingDaysPerWeek(event.target.value)
            }
            required
          />
        </label>

        <label className="edit-field">
          Træningsmål
          <select
            value={goal}
            onChange={(event) => setGoal(event.target.value)}
            required
          >
            <option value="MUSCLE_GAIN">Muskelmasse</option>
            <option value="STRENGTH">Styrke</option>
            <option value="WEIGHT_LOSS">Vægttab</option>
            <option value="GENERAL_FITNESS">Generel form</option>
          </select>
        </label>

        <button type="submit">Gem ændringer</button>

        <button type="button" onClick={onCancel}>
          Annuller
        </button>

        {message && <p>{message}</p>}
      </form>
    </section>
  )
}

export default EditProfile