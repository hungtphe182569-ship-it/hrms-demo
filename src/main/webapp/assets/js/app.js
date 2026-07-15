(() => {
    const openModal = (id) => {
        const modal = document.getElementById(id);
        if (!modal) return;
        modal.classList.add('open');
        modal.setAttribute('aria-hidden', 'false');
        document.body.style.overflow = 'hidden';
        setTimeout(() => modal.querySelector('input, textarea, select, button')?.focus(), 20);
    };

    const closeModal = (modal) => {
        if (!modal) return;
        modal.classList.remove('open');
        modal.setAttribute('aria-hidden', 'true');
        document.body.style.overflow = '';
    };

    document.querySelectorAll('[data-open-modal]').forEach((button) => {
        button.addEventListener('click', () => openModal(button.dataset.openModal));
    });

    document.querySelectorAll('[data-close-modal]').forEach((button) => {
        button.addEventListener('click', () => closeModal(button.closest('.modal')));
    });

    document.querySelectorAll('.modal').forEach((modal) => {
        modal.addEventListener('click', (event) => {
            if (event.target === modal) closeModal(modal);
        });
    });

    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape') closeModal(document.querySelector('.modal.open'));
    });

    document.querySelectorAll('[data-confirm]').forEach((form) => {
        form.addEventListener('submit', (event) => {
            if (!window.confirm(form.dataset.confirm)) event.preventDefault();
        });
    });

    document.querySelectorAll('[data-delete-account]').forEach((button) => {
        button.addEventListener('click', () => {
            document.getElementById('deleteAccountId').value = button.dataset.deleteAccount;
            document.getElementById('deleteAccountName').textContent = '@' + button.dataset.accountName;
            openModal('deleteAccountModal');
        });
    });

    document.querySelectorAll('[data-edit-role]').forEach((button) => {
        button.addEventListener('click', () => {
            document.getElementById('editRoleId').value = button.dataset.editRole;
            document.getElementById('editRoleName').value = button.dataset.roleName || '';
            document.getElementById('editRoleDescription').value = button.dataset.roleDescription || '';
            openModal('editRoleModal');
        });
    });

    document.querySelectorAll('[data-edit-account]').forEach((button) => {
        button.addEventListener('click', () => {
            document.getElementById('editAccountId').value = button.dataset.editAccount;
            document.getElementById('editAccountUsername').value = button.dataset.username || '';
            document.getElementById('editAccountEmail').value = button.dataset.email || '';
            document.getElementById('editAccountFullName').value = button.dataset.fullName || '';
            document.getElementById('editAccountPhone').value = button.dataset.phone || '';
            const selected = new Set((button.dataset.roleIds || '').split(','));
            document.querySelectorAll('#editAccountRoles option').forEach(option => {
                option.selected = selected.has(option.value);
            });
            openModal('editAccountModal');
        });
    });

    const bindSearch = (inputId, selector) => {
        const input = document.getElementById(inputId);
        if (!input) return;
        const items = [...document.querySelectorAll(selector)];
        input.addEventListener('input', () => {
            const term = input.value.trim().toLocaleLowerCase('vi');
            items.forEach((item) => {
                item.hidden = !item.dataset.search.toLocaleLowerCase('vi').includes(term);
            });
        });
    };

    bindSearch('accountSearch', '#accountTable tbody tr');
    bindSearch('roleSearch', '#roleGrid .role-card');

    document.getElementById('includeDeleted')?.addEventListener('change', (event) => {
        const url = new URL(window.location.href);
        if (event.target.checked) url.searchParams.set('includeDeleted', 'true');
        else url.searchParams.delete('includeDeleted');
        window.location.href = url.toString();
    });

    document.querySelectorAll('[data-chart]').forEach((chart) => {
        const bars = [...chart.querySelectorAll('[data-value]')];
        const max = Math.max(1, ...bars.map((bar) => Number(bar.dataset.value)));
        requestAnimationFrame(() => bars.forEach((bar) => {
            bar.style.width = `${Math.max(6, Number(bar.dataset.value) / max * 100)}%`;
        }));
    });

    document.querySelectorAll('form[data-validate="account"]').forEach((form) => {
        form.addEventListener('submit', (event) => {
            if (!form.checkValidity()) {
                event.preventDefault();
                form.reportValidity();
            }
        });
    });
})();
document.querySelectorAll('[data-toggle-password]').forEach(button => {
    button.addEventListener('click', () => {
        const input = document.getElementById(button.dataset.togglePassword);
        const show = input.type === 'password';
        input.type = show ? 'text' : 'password';
        button.textContent = show ? 'Ẩn' : 'Hiện';
    });
});
