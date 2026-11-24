import { useEffect, useState } from 'react'
import EditProfile from '../EditProfile/EditProfile'
import WorkoutProgram from '../WorkoutProgram/WorkoutProgram'
import AiCoach from '../AiCoach/AiCoach'
import './Profile.css'

function Profile({ token, onLogout }) {
  const [user, setUser] = useState(null)
  const [editing, setEditing] = useState(false)
  const [message, setMessage] = useState('')

  useEffect(() => {
    async function getCurrentUser() {
      const response = await fetch('/api/users/me', {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })

      if (!response.ok) {
        setMessage('Kunne ikke hente brugerens profil')
        return
      }

      const data = await response.json()
      setUser(data)
    }

    getCurrentUser()
  }, [token])

  function handleUpdatedUser(updatedUser) {
    setUser(updatedUser)
    setEditing(false)
  }

  if (message) {
    return <p>{message}</p>
  }

  if (!user) {
    return <p>Henter profil...</p>
  }

  if (editing) {
    return (
      <EditProfile
        user={user}
        token={token}
        onUpdated={handleUpdatedUser}
        onCancel={() => setEditing(false)}
      />
    )
  }

  return (
    <>
      <section className="profile-panel">
        <h1>Min profil</h1>

        <p>Navn: {user.name}</p>
        <p>E-mail: {user.email}</p>
        <p>Alder: {user.age}</p>
        <p>Højde: {user.height} cm</p>
        <p>Vægt: {user.weight} kg</p>
        <p>Erfaringsniveau: {user.experienceLevel}</p>
        <p>Træningsdage om ugen: {user.trainingDaysPerWeek}</p>
        <p>Træningsmål: {user.goal}</p>

        <button type="button" onClick={() => setEditing(true)}>
          Rediger profil
        </button>

        <button type="button" onClick={onLogout}>
          Log ud
        </button>
      </section>

      <WorkoutProgram
        userId={user.id}
        token={token}
        workoutProgramId={user.workoutProgramId}
      />

      <AiCoach token={token} />
    </>
  )
}

export default Profile