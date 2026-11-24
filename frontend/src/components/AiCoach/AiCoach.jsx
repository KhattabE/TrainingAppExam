import { useState } from 'react'
import './AiCoach.css'

function AiCoach({ token }) {
  const [prompt, setPrompt] = useState('')
  const [answer, setAnswer] = useState('')
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')

  async function handleSubmit(event) {
    event.preventDefault()

    setLoading(true)
    setMessage('')
    setAnswer('')

    const response = await fetch('/api/coach', {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        prompt,
      }),
    })

    if (!response.ok) {
      setMessage('Kunne ikke få svar fra AI Coach')
      setLoading(false)
      return
    }

    const data = await response.json()

    setAnswer(data.answer)
    setLoading(false)
  }

  return (
    <section className="coach-panel">
      <h2>AI Coach</h2>

      <form onSubmit={handleSubmit}>
        <label className="coach-field">
          Spørg din coach
          <textarea
            value={prompt}
            onChange={(event) => setPrompt(event.target.value)}
            placeholder="Fx: Hvor lang pause skal jeg tage mellem tunge sæt?"
            required
          />
        </label>

        <button type="submit" disabled={loading}>
          {loading ? 'Henter svar...' : 'Spørg AI Coach'}
        </button>
      </form>

      {message && <p>{message}</p>}

      {answer && (
        <div className="coach-answer">
          <h3>Svar</h3>
          <p>{answer}</p>
        </div>
      )}
    </section>
  )
}

export default AiCoach