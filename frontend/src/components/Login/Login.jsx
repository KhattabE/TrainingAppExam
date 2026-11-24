import { useState } from 'react'
import './Login.css'

function Login({ onLogin }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [message, setMessage] = useState('')

  async function handleSubmit(event) {
    event.preventDefault()

    const response = await fetch('/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        email,
        password,
      }),
    })

    if (!response.ok) {
      setMessage('Login mislykkedes')
      return
    }

    const data = await response.json()

    onLogin(data.token)
  }

  return (
    <section
      className="account-panel login-panel"
      id="log-ind"
      aria-labelledby="logintitel"
    >
      <h1 id="logintitel">Log ind</h1>

      <form onSubmit={handleSubmit}>
        <fieldset>
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

          <button className="submit-button" type="submit">
            Log ind
          </button>

          {message && <p>{message}</p>}
        </fieldset>
      </form>
    </section>
  )
}

export default Login