import { useState, type FormEvent } from 'react';

export interface SearchCriteria {
  q: string;
  cuisine: string;
}

interface SearchBarProps {
  initial?: SearchCriteria;
  cuisines: string[];
  onSearch: (criteria: SearchCriteria) => void;
}

// Search input + cuisine filter. Drives GET /api/search via the parent page.
export function SearchBar({ initial, cuisines, onSearch }: SearchBarProps) {
  const [q, setQ] = useState(initial?.q ?? '');
  const [cuisine, setCuisine] = useState(initial?.cuisine ?? '');

  const submit = (e: FormEvent) => {
    e.preventDefault();
    onSearch({ q: q.trim(), cuisine });
  };

  return (
    <form className="dc-searchbar" onSubmit={submit} role="search">
      <input
        className="dc-searchbar__input"
        type="search"
        placeholder="Search restaurants or dishes…"
        value={q}
        onChange={(e) => setQ(e.target.value)}
        aria-label="Search query"
      />

      <select
        className="dc-searchbar__cuisine"
        value={cuisine}
        onChange={(e) => setCuisine(e.target.value)}
        aria-label="Cuisine filter"
      >
        <option value="">All cuisines</option>
        {cuisines.map((c) => (
          <option key={c} value={c}>
            {c}
          </option>
        ))}
      </select>

      <button type="submit" className="qb-btn dc-searchbar__submit">
        Search
      </button>
    </form>
  );
}
