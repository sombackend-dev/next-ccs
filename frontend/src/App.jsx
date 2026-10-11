
import { useEffect, useState } from 'react'
import './App.css'
import ReservationManager from './ReservationManager'

const PAGE_SIZE = 20

export default function App() {
  const [vehicles, setVehicles] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [form, setForm] = useState({
    vehicleNumber: '',
    modelName: '',
  })

  async function loadVehicles(pageNumber = page) {
    setLoading(true)
    setError('')

    try {
      const response = await fetch(
          `/vehicles?page=${pageNumber}&size=${PAGE_SIZE}`
      )

      if (!response.ok) {
        throw new Error(`차량 조회 실패 (HTTP ${response.status})`)
      }

      const data = await response.json()

      setVehicles(data.content ?? [])
      setTotalPages(data.totalPages ?? 0)
      setTotalElements(data.totalElements ?? 0)
      setPage(pageNumber)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadVehicles(0)
  }, [])

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setMessage('')

    if (!form.vehicleNumber.trim() || !form.modelName.trim()) {
      setError('차량번호와 모델명을 모두 입력해 주세요.')
      return
    }

    setSubmitting(true)

    try {
      const response = await fetch('/vehicles', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          vehicleNumber: form.vehicleNumber.trim(),
          modelName: form.modelName.trim(),
        }),
      })

      if (!response.ok) {
        let detail = ''
        try {
          const body = await response.json()
          detail = body.message || body.error || ''
        } catch {
          // 응답 본문이 JSON이 아닐 수 있음
        }

        throw new Error(
            detail || `차량 등록 실패 (HTTP ${response.status})`
        )
      }

      setForm({ vehicleNumber: '', modelName: '' })
      setMessage('차량이 성공적으로 등록되었습니다.')
      await loadVehicles(0)
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
      <div className="app-shell">
        <aside className="sidebar">
          <div className="brand">
            <span className="brand-icon">N</span>
            <div>
              <strong>Next-CCS</strong>
              <small>Connected Car System</small>
            </div>
          </div>

          <div className="sidebar-section">MANAGEMENT</div>
          <div className="nav-item active">🚘 차량 관리</div>

          <div className="sidebar-footer">
            React · Spring Boot · MySQL
          </div>
        </aside>

        <main className="main-content">
          <header className="topbar">
            <div>
              <span className="eyebrow">FLEET MANAGEMENT</span>
              <h1>차량 관리 대시보드</h1>
              <p>차량 정보를 조회하고 새로운 차량을 등록합니다.</p>
            </div>
            <span className="system-badge">● API Connected</span>
          </header>

          <section className="stats">
            <div className="stat-card">
              <span>전체 등록 차량</span>
              <strong>{totalElements.toLocaleString()}</strong>
              <small>데이터베이스 기준</small>
            </div>
            <div className="stat-card">
              <span>현재 페이지</span>
              <strong>{totalPages === 0 ? 0 : page + 1}</strong>
              <small>전체 {totalPages}페이지</small>
            </div>
            <div className="stat-card">
              <span>페이지당 조회</span>
              <strong>{PAGE_SIZE}</strong>
              <small>서버 페이지네이션</small>
            </div>
          </section>

          {error && <div className="alert error">{error}</div>}
          {message && <div className="alert success">{message}</div>}

          <section className="panel">
            <div className="panel-header">
              <div>
                <h2>차량 등록</h2>
                <p>차량번호와 모델명을 입력하세요.</p>
              </div>
            </div>

            <form className="vehicle-form" onSubmit={handleSubmit}>
              <label>
                차량번호
                <input
                    type="text"
                    placeholder="예: 12가3456"
                    value={form.vehicleNumber}
                    onChange={(e) =>
                        setForm({ ...form, vehicleNumber: e.target.value })
                    }
                />
              </label>

              <label>
                모델명
                <input
                    type="text"
                    placeholder="예: IONIQ 5"
                    value={form.modelName}
                    onChange={(e) =>
                        setForm({ ...form, modelName: e.target.value })
                    }
                />
              </label>

              <button type="submit" disabled={submitting}>
                {submitting ? '등록 중...' : '+ 차량 등록'}
              </button>
            </form>
          </section>

          <section className="panel">
            <div className="panel-header">
              <div>
                <h2>차량 목록</h2>
                <p>Spring Boot REST API에서 조회한 데이터입니다.</p>
              </div>
              <button
                  className="secondary-button"
                  onClick={() => loadVehicles(page)}
                  disabled={loading}
              >
                ↻ 새로고침
              </button>
            </div>

            <div className="table-container">
              <table>
                <thead>
                <tr>
                  <th>ID</th>
                  <th>차량번호</th>
                  <th>모델명</th>
                  <th>상태</th>
                </tr>
                </thead>
                <tbody>
                {loading ? (
                    <tr>
                      <td colSpan="4" className="empty-state">
                        차량 정보를 불러오는 중입니다...
                      </td>
                    </tr>
                ) : vehicles.length === 0 ? (
                    <tr>
                      <td colSpan="4" className="empty-state">
                        등록된 차량이 없습니다.
                      </td>
                    </tr>
                ) : (
                    vehicles.map((vehicle) => (
                        <tr key={vehicle.id}>
                          <td>#{vehicle.id}</td>
                          <td className="vehicle-number">
                            {vehicle.vehicleNumber}
                          </td>
                          <td>{vehicle.modelName}</td>
                          <td>
                        <span className="status-badge">
                          {vehicle.status}
                        </span>
                          </td>
                        </tr>
                    ))
                )}
                </tbody>
              </table>
            </div>

            <div className="pagination">
            <span>
              총 {totalElements.toLocaleString()}대
            </span>
              <div className="page-controls">
                <button
                    disabled={loading || page === 0}
                    onClick={() => loadVehicles(page - 1)}
                >
                  이전
                </button>
                <span>
                {totalPages === 0 ? 0 : page + 1} / {totalPages}
              </span>
                <button
                    disabled={loading || page + 1 >= totalPages}
                    onClick={() => loadVehicles(page + 1)}
                >
                  다음
                </button>
              </div>
            </div>
          </section>
          <ReservationManager vehicles={vehicles} />
        </main>
      </div>
  )
}
