import { useState } from 'react'
import './App.css'
import Navbar from './components/Navbar/Navbar'
import Login from './components/Login/Login'
import Register from './components/Register/Register'
import Profile from './components/Profile/Profile'

function App() {
  const [token, setToken] = useState(localStorage.getItem('token'))

  function handleLogin(newToken) {
    localStorage.setItem('token', newToken)
    setToken(newToken)
  }

  function handleLogout() {
    localStorage.removeItem('token')
    setToken(null)
  }

  return (
    <>
      <Navbar appName="TrainingApp" />

      <main className="account">
        {token ? (
          <Profile token={token} onLogout={handleLogout} />
        ) : (
          <>
            <Login onLogin={handleLogin} />
            <Register onRegister={handleLogin} />
          </>
        )}
      </main>
    </>
  )
}

export default App