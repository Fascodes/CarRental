import { useNavigate } from 'react-router-dom'

export default function ListingCard({ listing }) {
  const navigate = useNavigate()
  return (
    <div className="card" onClick={() => navigate(`/listing/${listing.id}`)}>
      <div className="img-placeholder" style={{ height: 120, marginBottom: '0.75rem' }}>Brak zdjęcia</div>
      <h3>{listing.title}</h3>
      <div className="price">{listing.price} zł / dzień</div>
      <div className="meta">
        {listing.brand} {listing.model} · {listing.modelYear} · {listing.gearboxType}
      </div>
      {listing.localization && (
        <div className="meta" style={{ marginTop: '0.25rem' }}>📍 {listing.localization}</div>
      )}
    </div>
  )
}
