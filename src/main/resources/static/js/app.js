document.addEventListener("DOMContentLoaded", function() {
    
    // Mobile Sidebar Toggle
    const mobileMenuBtn = document.getElementById('mobileMenuBtn');
    const sidebar = document.querySelector('.sidebar');
    const mobileNavBackdrop = document.getElementById('mobileNavBackdrop');
    
    if (mobileMenuBtn && sidebar) {
        const closeMobileMenu = function() {
            sidebar.classList.remove('show');
            mobileMenuBtn.setAttribute('aria-expanded', 'false');
            mobileMenuBtn.setAttribute('aria-label', 'Open navigation menu');
            if (mobileNavBackdrop) {
                mobileNavBackdrop.classList.remove('show');
                mobileNavBackdrop.setAttribute('aria-hidden', 'true');
            }
        };

        mobileMenuBtn.addEventListener('click', function() {
            const isOpen = sidebar.classList.toggle('show');
            mobileMenuBtn.setAttribute('aria-expanded', isOpen);
            mobileMenuBtn.setAttribute('aria-label', isOpen ? 'Close navigation menu' : 'Open navigation menu');
            if (mobileNavBackdrop) {
                mobileNavBackdrop.classList.toggle('show', isOpen);
                mobileNavBackdrop.setAttribute('aria-hidden', String(!isOpen));
            }
        });

        sidebar.querySelectorAll('.nav-link').forEach(link => {
            link.addEventListener('click', closeMobileMenu);
        });

        if (mobileNavBackdrop) {
            mobileNavBackdrop.addEventListener('click', closeMobileMenu);
        }

        document.addEventListener('keydown', function(event) {
            if (event.key === 'Escape' && sidebar.classList.contains('show')) {
                closeMobileMenu();
                mobileMenuBtn.focus();
            }
        });
    }

    // Delete Confirmation
    const deleteForms = document.querySelectorAll('.delete-form');
    deleteForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            if (!confirm('Are you sure you want to delete this item? This action cannot be undone.')) {
                e.preventDefault();
            }
        });
    });

    // Auto-dismiss alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert:not(.alert-danger)');
    alerts.forEach(alert => {
        setTimeout(() => {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });
});
