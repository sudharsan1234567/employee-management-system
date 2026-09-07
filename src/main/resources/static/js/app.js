document.addEventListener("DOMContentLoaded", function() {
    
    // Mobile Sidebar Toggle
    const mobileMenuBtn = document.getElementById('mobileMenuBtn');
    const sidebar = document.querySelector('.sidebar');
    
    if (mobileMenuBtn && sidebar) {
        mobileMenuBtn.addEventListener('click', function() {
            const isOpen = sidebar.classList.toggle('show');
            mobileMenuBtn.setAttribute('aria-expanded', isOpen);
            mobileMenuBtn.setAttribute('aria-label', isOpen ? 'Close navigation menu' : 'Open navigation menu');
        });

        sidebar.querySelectorAll('.nav-link').forEach(link => {
            link.addEventListener('click', function() {
                sidebar.classList.remove('show');
                mobileMenuBtn.setAttribute('aria-expanded', 'false');
                mobileMenuBtn.setAttribute('aria-label', 'Open navigation menu');
            });
        });

        document.addEventListener('click', function(event) {
            if (sidebar.classList.contains('show') &&
                !sidebar.contains(event.target) &&
                !mobileMenuBtn.contains(event.target)) {
                sidebar.classList.remove('show');
                mobileMenuBtn.setAttribute('aria-expanded', 'false');
                mobileMenuBtn.setAttribute('aria-label', 'Open navigation menu');
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
