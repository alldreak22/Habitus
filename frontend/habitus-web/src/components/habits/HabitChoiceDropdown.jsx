import { useEffect, useId, useState } from 'react';

const DROPDOWN_OPEN_EVENT = 'habitus-dropdown-open';

export default function HabitChoiceDropdown({ children, label, renderValue }) {
  const [isOpen, setIsOpen] = useState(false);
  const dropdownId = useId();

  useEffect(() => {
    function handleDropdownOpen(event) {
      if (event.detail !== dropdownId) {
        setIsOpen(false);
      }
    }

    window.addEventListener(DROPDOWN_OPEN_EVENT, handleDropdownOpen);

    return () => {
      window.removeEventListener(DROPDOWN_OPEN_EVENT, handleDropdownOpen);
    };
  }, [dropdownId]);

  function closeDropdown() {
    setIsOpen(false);
  }

  function handleToggle() {
    if (!isOpen) {
      window.dispatchEvent(new CustomEvent(DROPDOWN_OPEN_EVENT, { detail: dropdownId }));
    }

    setIsOpen((currentState) => !currentState);
  }

  return (
    <div className="habit-choice-dropdown">
      <button
        className={isOpen ? 'habit-choice-trigger active' : 'habit-choice-trigger'}
        type="button"
        aria-expanded={isOpen}
        aria-controls={dropdownId}
        onClick={handleToggle}
      >
        {renderValue()}
        <span className="material-symbols-outlined" aria-hidden="true">
          expand_more
        </span>
      </button>
      {isOpen ? (
        <div id={dropdownId} className="habit-choice-popover" role="group" aria-label={label}>
          {children(closeDropdown)}
        </div>
      ) : null}
    </div>
  );
}
