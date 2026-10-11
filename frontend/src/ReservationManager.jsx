
import { useState } from 'react'

function toLocalInputValue(date) {
    const pad = (value) => String(value).padStart(2, '0')

    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`
}

export default function ReservationManager({ vehicles }) {
    const [vehicleId, setVehicleId] = useState('')
    const [startTime, setStartTime] = useState('')
    const [endTime, setEndTime] = useState('')
    const [reservations, setReservations] = useState([])
    const [loading, setLoading] = useState(false)
    const [submitting, setSubmitting] = useState(false)
    const [error, setError] = useState('')
    const [message, setMessage] = useState('')

    async function readError(response) {
        try {
            const body = await response.json()
            return body.message || body.error || `HTTP ${response.status}`
        } catch {
            return `HTTP ${response.status}`
        }
    }

    async function searchReservations() {
        setError('')
        setMessage('')

        if (!vehicleId || !startTime || !endTime) {
            setError('차량과 조회 시작·종료 시간을 입력해 주세요.')
            return
        }

        if (startTime >= endTime) {
            setError('종료 시간은 시작 시간보다 늦어야 합니다.')
            return
        }

        setLoading(true)

        try {
            const params = new URLSearchParams({
                vehicleId,
                startTime: `${startTime}:00`,
                endTime: `${endTime}:00`,
            })

            const response = await fetch(`/reservations?${params}`)

            if (!response.ok) {
                throw new Error(await readError(response))
            }

            const data = await response.json()
            setReservations(data)
            setMessage(`${data.length}건의 예약을 조회했습니다.`)
        } catch (err) {
            setError(err.message)
            setReservations([])
        } finally {
            setLoading(false)
        }
    }

    async function createReservation() {
        setError('')
        setMessage('')

        if (!vehicleId || !startTime || !endTime) {
            setError('차량과 예약 시작·종료 시간을 입력해 주세요.')
            return
        }

        if (startTime >= endTime) {
            setError('종료 시간은 시작 시간보다 늦어야 합니다.')
            return
        }

        setSubmitting(true)

        try {
            const response = await fetch('/reservations', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    vehicleId: Number(vehicleId),
                    startTime: `${startTime}:00`,
                    endTime: `${endTime}:00`,
                }),
            })

            if (!response.ok) {
                if (response.status === 409) {
                    throw new Error('이미 예약된 시간입니다. 다른 시간을 선택해 주세요.')
                }

                throw new Error(await readError(response))
            }

            setMessage('예약이 성공적으로 생성되었습니다.')
            await searchReservations()
        } catch (err) {
            setError(err.message)
        } finally {
            setSubmitting(false)
        }
    }

    return (
        <section className="panel">
            <div className="panel-header">
                <div>
                    <h2>예약 관리</h2>
                    <p>차량별 예약 조회 및 예약 생성</p>
                </div>
            </div>

            <div className="reservation-form">
                <label>
                    차량 선택
                    <select
                        value={vehicleId}
                        onChange={(e) => setVehicleId(e.target.value)}
                    >
                        <option value="">차량을 선택하세요</option>
                        {vehicles.map((vehicle) => (
                            <option key={vehicle.id} value={vehicle.id}>
                                {vehicle.vehicleNumber} ({vehicle.modelName})
                            </option>
                        ))}
                    </select>
                </label>

                <label>
                    시작 시간
                    <input
                        type="datetime-local"
                        value={startTime}
                        onChange={(e) => setStartTime(e.target.value)}
                    />
                </label>

                <label>
                    종료 시간
                    <input
                        type="datetime-local"
                        value={endTime}
                        onChange={(e) => setEndTime(e.target.value)}
                    />
                </label>

                <div className="reservation-actions">
                    <button onClick={searchReservations} disabled={loading || submitting}>
                        {loading ? '조회 중...' : '예약 조회'}
                    </button>
                    <button
                        className="primary-button"
                        onClick={createReservation}
                        disabled={loading || submitting}
                    >
                        {submitting ? '예약 중...' : '+ 예약 생성'}
                    </button>
                </div>
            </div>

            {error && <div className="reservation-alert error">{error}</div>}
            {message && <div className="reservation-alert success">{message}</div>}

            <div className="table-container">
                <table>
                    <thead>
                    <tr>
                        <th>예약 ID</th>
                        <th>차량 ID</th>
                        <th>시작 시간</th>
                        <th>종료 시간</th>
                        <th>상태</th>
                    </tr>
                    </thead>
                    <tbody>
                    {reservations.length === 0 ? (
                        <tr>
                            <td colSpan="5" className="empty-state">
                                조회된 예약이 없습니다.
                            </td>
                        </tr>
                    ) : (
                        reservations.map((reservation) => (
                            <tr key={reservation.id}>
                                <td>#{reservation.id}</td>
                                <td>#{reservation.vehicleId}</td>
                                <td>{reservation.startTime.replace('T', ' ')}</td>
                                <td>{reservation.endTime.replace('T', ' ')}</td>
                                <td>
                    <span className="status-badge">
                      {reservation.status}
                    </span>
                                </td>
                            </tr>
                        ))
                    )}
                    </tbody>
                </table>
            </div>
        </section>
    )
}
