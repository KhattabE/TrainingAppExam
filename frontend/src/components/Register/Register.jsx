import { useState } from 'react'
import './Register.css'

function Register({ onRegister }) {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [age, setAge] = useState('')
  const [height, setHeight] = useState('')
  const [weight, setWeight] = useState('')
  const [experienceLevel, setExperienceLevel] = useState('')
  const [trainingDaysPerWeek, setTrainingDaysPerWeek] = useState('')
  const [goal, setGoal] = useState('')
  const [message, setMessage] = useState('')

  async function handleSubmit(event) {
    event.preventDefault()

    const response = await fetch('/auth/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        name,
        email,
        password,
        age: Number(age),
        height: Number(height),
        weight: Number(weight),
        experienceLevel,
        trainingDaysPerWeek: Number(trainingDaysPerWeek),
        goal,
      }),
    })

    if (!response.ok) {
      setMessage('Oprettelse mislykkedes')
      return
    }

    const data = await response.json()

    onRegister(data.token)
  }

  return (
    <section
      className="account-panel register-panel"
      id="opret-konto"
      aria-labelledby="registreringstitel"
    >
      <h1 id="registreringstitel">Opret konto</h1>

      <form onSubmit={handleSubmit}>
        <fieldset>
          <label className="field">
            Navn
            <input
              type="text"
              name="name"
              value={name}
              onChange={(event) => setName(event.target.value)}
              required
            />
          </label>

          <label className="field">
            E-mail
            <input
              type="email"
              name="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
            />
          </label>

          <label className="field">
            Adgangskode
            <input
              type="password"
              name="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </label>

          <label className="field">
            Alder (år)
            <input
              type="number"
              name="age"
              min="1"
              value={age}
              onChange={(event) => setAge(event.target.value)}
              required
            />
          </label>

          <label className="field">
            Højde (cm)
            <input
              type="number"
              name="height"
              min="0.1"
              step="0.1"
              value={height}
              onChange={(event) => setHeight(event.target.value)}
              required
            />
          </label>

          <label className="field">
            Vægt (kg)
            <input
              type="number"
              name="weight"
              min="0.1"
              step="0.1"
              value={weight}
              onChange={(event) => setWeight(event.target.value)}
              required
            />
          </label>

          <label className="field">
            Erfaringsniveau
            <select
              name="experienceLevel"
              value={experienceLevel}
              onChange={(event) => setExperienceLevel(event.target.value)}
              required
            >
              <option value="">Vælg niveau</option>
              <option value="BEGINNER">Begynder</option>
              <option value="INTERMEDIATE">Let øvet</option>
              <option value="ADVANCED">Øvet</option>
            </select>
          </label>

          <label className="field">
            Træningsdage om ugen
            <input
              type="number"
              name="trainingDaysPerWeek"
              min="1"
              max="7"
              value={trainingDaysPerWeek}
              onChange={(event) =>
                setTrainingDaysPerWeek(event.target.value)
              }
              required
            />
          </label>

          <label className="field">
            Træningsmål
            <select
              name="goal"
              value={goal}
              onChange={(event) => setGoal(event.target.value)}
              required
            >
              <option value="">Vælg mål</option>
              <option value="MUSCLE_GAIN">Muskelmasse</option>
              <option value="STRENGTH">Styrke</option>
              <option value="WEIGHT_LOSS">Vægttab</option>
              <option value="GENERAL_FITNESS">Generel form</option>
            </select>
          </label>

          <button className="submit-button" type="submit">
            Opret konto
          </button>

          {message && <p>{message}</p>}
        </fieldset>
      </form>
    </section>
  )
}

export default Register