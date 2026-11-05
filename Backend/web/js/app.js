/* CINEVERSE — script dùng chung. Trang vẫn hoạt động đầy đủ khi tắt JS (trừ phần tóm tắt ghế/hộp thoại). */
(function () {
    'use strict';

    function money(n) {
        return n.toLocaleString('vi-VN') + ' đ';
    }

    // Hỏi xác nhận trước khi gửi form có thuộc tính data-confirm="..."
    document.addEventListener('submit', function (e) {
        var msg = e.target.getAttribute && e.target.getAttribute('data-confirm');
        if (msg && !window.confirm(msg)) {
            e.preventDefault();
        }
    });

    // Thông báo tự ẩn sau vài giây hoặc khi bấm vào
    document.querySelectorAll('[data-autohide]').forEach(function (el) {
        el.addEventListener('click', function () { el.remove(); });
        setTimeout(function () { el.remove(); }, 7000);
    });

    // ---------- Trang chọn ghế ----------
    var booking = document.getElementById('bookingForm');
    if (booking) {
        var price = Number(booking.getAttribute('data-price')) || 0;
        var max = Number(booking.getAttribute('data-max')) || 8;
        var seats = booking.querySelectorAll('.seat input');
        var list = document.getElementById('pickedSeats');
        var count = document.getElementById('pickedCount');
        var total = document.getElementById('pickedTotal');
        var submit = document.getElementById('bookingSubmit');
        var hint = document.getElementById('bookingHint');
        var defaultHint = hint ? hint.textContent : '';

        var refresh = function () {
            var picked = [];
            seats.forEach(function (s) {
                if (s.checked) { picked.push(s.getAttribute('data-name')); }
            });
            list.textContent = picked.length ? picked.join(', ') : 'Chưa chọn';
            count.textContent = picked.length;
            total.textContent = money(picked.length * price);
            if (submit && !submit.hasAttribute('data-locked')) {
                submit.disabled = picked.length === 0;
            }
            if (hint) {
                hint.textContent = picked.length >= max ? 'Đã chọn tối đa ' + max + ' ghế.' : defaultHint;
            }
            // Đủ số ghế tối đa thì khoá các ghế còn lại
            seats.forEach(function (s) {
                if (!s.checked && !s.hasAttribute('data-taken')) {
                    s.disabled = picked.length >= max;
                }
            });
        };
        seats.forEach(function (s) { s.addEventListener('change', refresh); });
        refresh();
    }

    // ---------- Quản trị: hộp thoại thêm / sửa phim ----------
    var dialog = document.getElementById('movieDialog');
    if (dialog && typeof dialog.showModal === 'function') {
        var form = document.getElementById('movieForm');
        var title = document.getElementById('movieDialogTitle');
        var submitBtn = document.getElementById('movieSubmitBtn');

        dialog.querySelectorAll('[data-close]').forEach(function (b) {
            b.addEventListener('click', function () { dialog.close(); });
        });

        document.querySelectorAll('.js-movie-new').forEach(function (btn) {
            btn.addEventListener('click', function () {
                form.reset();
                form.elements.action.value = 'addMovie';
                form.elements.movieID.value = '';
                title.textContent = 'Thêm phim mới';
                submitBtn.textContent = 'Lưu phim';
                dialog.showModal();
            });
        });

        document.querySelectorAll('.js-movie-edit').forEach(function (btn) {
            btn.addEventListener('click', function () {
                var d = btn.dataset;
                form.reset();
                form.elements.action.value = 'updateMovie';
                form.elements.movieID.value = d.id;
                form.elements.movieName.value = d.name || '';
                form.elements.director.value = d.director || '';
                form.elements.actor.value = d.actor || '';
                form.elements.duration.value = d.duration || '';
                form.elements.ageRating.value = d.age || '';
                form.elements.ratingScore.value = d.rating || '';
                form.elements.language.value = d.language || '';
                form.elements.bio.value = d.bio || '';
                form.elements.posterURL.value = d.poster || '';
                form.elements.trailerURL.value = d.trailer || '';
                form.elements.releaseDate.value = (d.release || '').substring(0, 10);
                var ids = (d.genreIds || '').split(',');
                form.querySelectorAll('input[name="genreIDs"]').forEach(function (cb) {
                    cb.checked = ids.indexOf(cb.value) !== -1;
                });
                title.textContent = 'Sửa phim #' + d.id;
                submitBtn.textContent = 'Cập nhật';
                dialog.showModal();
            });
        });
    }
})();
