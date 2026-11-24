import './Navbar.css'

function Navbar({ appName }) {
  return (
    <header className="site-header">
      <div className="header-content">
        <a className="brand" href="#" aria-label="TrainingApp forside">
          <span className="brand-mark" aria-hidden="true">T</span>
          {appName}
        </a>

        <nav aria-label="Konto">
          <a href="#log-ind">Log ind</a>
          <a href="#opret-konto">Opret konto</a>
        </nav>
      </div>
    </header>
  )
}

export default Navbar