import { useState } from 'react';
import { Link } from 'react-router-dom';
import { api, errorMessage } from '../api';

const suggestions = ['What should I do in an emergency?', 'How do safety zones work?', 'What should I do if my passport is stolen?'];

export default function SafetyAssistant() {
  const [message, setMessage] = useState('');
  const [answer, setAnswer] = useState(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const ask = async (question = message) => {
    const prompt = question.trim();
    if (!prompt || busy) return;
    setMessage(prompt);
    setBusy(true);
    setError('');
    setAnswer(null);
    try {
      const { data } = await api.post('/api/assistant', { message: prompt });
      setAnswer(data);
    } catch (requestError) {
      setError(errorMessage(requestError));
    } finally {
      setBusy(false);
    }
  };

  const submit = (event) => { event.preventDefault(); ask(); };

  return <main className="dashboard">
    <section className="profile-area assistant-page">
      <div className="section-title"><div><span className="eyebrow">TRAVEL GUIDANCE</span><h1>Safety Assistant</h1></div><Link className="secondary-link" to="/dashboard">Back to dashboard</Link></div>
      <p className="page-intro">Ask about practical precautions, emergency guidance, geofencing, or YatraSecure features. General guidance can’t replace current local advice.</p>
      <div className="assistant-emergency-note"><strong>Immediate danger?</strong> Contact local emergency services directly. YatraSecure SOS records an in-app alert and does not dispatch responders.</div>

      <section className="assistant-chat-panel">
        <div className="assistant-response-area" aria-live="polite">
          {!answer && !busy && <div className="assistant-empty"><span className="dashboard-icon icon-violet" aria-hidden="true">✦</span><h2>How can I help with your trip?</h2><p>Choose a common question or write your own.</p><div className="assistant-suggestions">{suggestions.map((suggestion) => <button type="button" key={suggestion} onClick={() => ask(suggestion)}>{suggestion}<span aria-hidden="true">→</span></button>)}</div></div>}
          {busy && <div className="assistant-loading" role="status">Preparing travel guidance…</div>}
          {error && <div className="notice error" role="alert">{error}</div>}
          {answer && <article className="assistant-answer"><div className="assistant-answer-label"><span className="dashboard-icon icon-violet" aria-hidden="true">✦</span><span>{answer.source === 'local' ? 'LOCAL SAFETY GUIDE' : 'SAFETY ASSISTANT'}</span></div><p>{answer.message}</p></article>}
        </div>
        <form className="assistant-prompt-form" onSubmit={submit}>
          <label htmlFor="assistant-question">Your question</label>
          <div><textarea id="assistant-question" required maxLength="2000" value={message} onChange={(event) => setMessage(event.target.value)} placeholder="For example: What precautions should I take when travelling alone?" /><button className="primary-btn" disabled={busy || !message.trim()}>{busy ? 'Thinking…' : 'Get guidance'} <span aria-hidden="true">→</span></button></div>
        </form>
      </section>
    </section>
  </main>;
}
