// CINEVERSE Interactive Frontend Logic (matching Backend Models & Enums)

document.addEventListener('DOMContentLoaded', () => {
    // 1. Movie Filter (Home Page)
    const statusTabs = document.querySelectorAll('.status-tab');
    const genrePills = document.querySelectorAll('.genre-pill');
    const movieCards = document.querySelectorAll('.movie-card');
    const searchInput = document.getElementById('movieSearch');

    let activeStatus = 'ALL';
    let activeGenre = 'ALL';

    function filterMovies() {
        const query = (searchInput ? searchInput.value : '').toLowerCase().trim();
        movieCards.forEach(card => {
            const cardStatus = card.dataset.status || 'PLAYING';
            const cardGenres = (card.dataset.genres || '').split(',');
            const cardTitle = (card.dataset.title || '').toLowerCase();

            const matchStatus = activeStatus === 'ALL' || cardStatus === activeStatus;
            const matchGenre = activeGenre === 'ALL' || cardGenres.includes(activeGenre);
            const matchSearch = !query || cardTitle.includes(query);

            card.style.display = (matchStatus && matchGenre && matchSearch) ? 'flex' : 'none';
        });
    }

    statusTabs.forEach(tab => {
        tab.addEventListener('click', () => {
            statusTabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            activeStatus = tab.dataset.filterStatus || 'ALL';
            filterMovies();
        });
    });

    genrePills.forEach(pill => {
        pill.addEventListener('click', () => {
            genrePills.forEach(p => p.classList.remove('active'));
            pill.classList.add('active');
            activeGenre = pill.dataset.genre || 'ALL';
            filterMovies();
        });
    });

    if (searchInput) {
        searchInput.addEventListener('input', filterMovies);
    }

    // 2. Interactive Seat Map (Booking Page)
    const seatMapContainer = document.getElementById('seatMapGrid');
    if (seatMapContainer) {
        const rows = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
        const cols = 12;
        const vipRows = ['D', 'E', 'F'];
        const occupiedSeats = new Set(['B5', 'B6', 'C7', 'C8', 'D4', 'E8', 'E9', 'F3', 'G6', 'G7']);
        const selectedSeats = new Set(['F6', 'F7']);

        function renderSeats() {
            seatMapContainer.innerHTML = '';
            rows.forEach(r => {
                const rowEl = document.createElement('div');
                rowEl.className = 'seat-row';

                const labelLeft = document.createElement('span');
                labelLeft.className = 'row-label';
                labelLeft.textContent = r;
                rowEl.appendChild(labelLeft);

                for (let c = 1; c <= cols; c++) {
                    const seatCode = `${r}${c}`;
                    const seatBtn = document.createElement('div');
                    seatBtn.className = 'seat';
                    seatBtn.textContent = seatCode;

                    if (vipRows.includes(r)) {
                        seatBtn.classList.add('vip');
                    }
                    if (occupiedSeats.has(seatCode)) {
                        seatBtn.classList.add('occupied');
                    } else if (selectedSeats.has(seatCode)) {
                        seatBtn.classList.add('selected');
                    }

                    seatBtn.addEventListener('click', () => {
                        if (occupiedSeats.has(seatCode)) return;
                        if (selectedSeats.has(seatCode)) {
                            selectedSeats.delete(seatCode);
                        } else {
                            selectedSeats.add(seatCode);
                        }
                        renderSeats();
                        updateBookingSummary();
                    });

                    rowEl.appendChild(seatBtn);
                }

                const labelRight = document.createElement('span');
                labelRight.className = 'row-label';
                labelRight.textContent = r;
                rowEl.appendChild(labelRight);

                seatMapContainer.appendChild(rowEl);
            });
        }

        function updateBookingSummary() {
            const seatsEl = document.getElementById('selectedSeatsText');
            const ticketsPriceEl = document.getElementById('ticketsPriceText');
            const totalAmountEl = document.getElementById('totalAmountText');

            const arr = Array.from(selectedSeats).sort();
            let subtotal = 0;
            arr.forEach(code => {
                const r = code.charAt(0);
                subtotal += vipRows.includes(r) ? 14 : 10;
            });
            const tax = arr.length > 0 ? 4 : 0;
            const total = subtotal + tax;

            if (seatsEl) seatsEl.textContent = arr.length > 0 ? arr.join(', ') : 'Chưa chọn';
            if (ticketsPriceEl) ticketsPriceEl.textContent = `$${subtotal.toFixed(2)}`;
            if (totalAmountEl) totalAmountEl.textContent = `$${total.toFixed(2)}`;
        }

        renderSeats();
        updateBookingSummary();

        // Date & Showtime slot buttons
        document.querySelectorAll('.date-strip .slot-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('.date-strip .slot-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
            });
        });

        document.querySelectorAll('.showtime-strip .slot-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('.showtime-strip .slot-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                const roomLabel = document.getElementById('summaryRoomText');
                if (roomLabel && btn.dataset.room) {
                    roomLabel.textContent = btn.dataset.room;
                }
            });
        });
    }

    // 3. BookingStatus Tabs (Tickets Page)
    const bookingTabs = document.querySelectorAll('[data-booking-tab]');
    const ticketItems = document.querySelectorAll('[data-booking-status]');
    bookingTabs.forEach(tab => {
        tab.addEventListener('click', () => {
            bookingTabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            const target = tab.dataset.bookingTab;
            ticketItems.forEach(item => {
                item.style.display = (target === 'ALL' || item.dataset.bookingStatus === target) ? 'grid' : 'none';
            });
        });
    });

    // 4. Modals
    document.querySelectorAll('[data-open-modal]').forEach(trigger => {
        trigger.addEventListener('click', () => {
            const id = trigger.dataset.openModal;
            const modal = document.getElementById(id);
            if (modal) modal.classList.add('open');
        });
    });

    document.querySelectorAll('[data-close-modal]').forEach(closeBtn => {
        closeBtn.addEventListener('click', () => {
            closeBtn.closest('.modal-backdrop').classList.remove('open');
        });
    });
});
