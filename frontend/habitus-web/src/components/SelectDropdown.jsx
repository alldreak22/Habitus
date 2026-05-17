import { useEffect, useId, useState } from 'react';

const DROPDOWN_OPEN_EVENT = 'habitus-dropdown-open';

export default function SelectDropdown({ label, onChange, options, value }) {
  const [isOpen, setIsOpen] = useState(false);
  const listId = useId();
  const selectedOption = options.find((option) => option.value === value) ?? options[0];

  useEffect(() => {
    function handleDropdownOpen(event) {
      if (event.detail !== listId) {
        setIsOpen(false);
      }
    }

    window.addEventListener(DROPDOWN_OPEN_EVENT, handleDropdownOpen);

    return () => {
      window.removeEventListener(DROPDOWN_OPEN_EVENT, handleDropdownOpen);
    };
  }, [listId]);

  function handleSelect(nextValue) {
    onChange(nextValue);
    setIsOpen(false);
  }

  function handleToggle() {
    if (!isOpen) {
      window.dispatchEvent(new CustomEvent(DROPDOWN_OPEN_EVENT, { detail: listId }));
    }

    setIsOpen((currentState) => !currentState);
  }

  return (
    <div className="select-dropdown">
      <button
        className={isOpen ? 'select-dropdown-trigger active' : 'select-dropdown-trigger'}
        type="button"
        aria-expanded={isOpen}
        aria-controls={listId}
        aria-label={label}
        onClick={handleToggle}
      >
        <span>{selectedOption?.label}</span>
        <span className="material-symbols-outlined" aria-hidden="true">
          expand_more
        </span>
      </button>
      {isOpen ? (
        <div id={listId} className="select-dropdown-menu" role="listbox" aria-label={label}>
          {options.map((option) => (
            <button
              key={option.value}
              className={option.value === value ? 'selected' : ''}
              type="button"
              role="option"
              aria-selected={option.value === value}
              onClick={() => handleSelect(option.value)}
            >
              {option.label}
            </button>
          ))}
        </div>
      ) : null}
    </div>
  );
}
