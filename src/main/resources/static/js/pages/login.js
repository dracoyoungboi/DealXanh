/**
 * Login Page JavaScript
 */

// Import utilities (if using modules)
// import { showToast, validators, storage } from '../utils.js';
// import { authApi } from '../api.js';

// Simple import for standalone HTML
const showToast = (message, type = 'success') => {
    const toast = document.createElement('div');
    toast.className = `toast toast--${type}`;
    toast.innerHTML = `
        <div style="display: flex; align-items: center; gap: 12px;">
            <span style="font-size: 14px; font-weight: 500;">${message}</span>
        </div>
    `;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
};

const validators = {
    email(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    },
    phone(phone) {
        const re = /(84|0[3|5|7|8|9])+([0-9]{8})\b/;
        return re.test(phone);
    },
    required(value) {
        return value && value.trim() !== '';
    }
};

const storage = {
    get(key, defaultValue = null) {
        try {
            const item = localStorage.getItem(key);
            return item ? JSON.parse(item) : defaultValue;
        } catch {
            return defaultValue;
        }
    },
    set(key, value) {
        try {
            localStorage.setItem(key, JSON.stringify(value));
        } catch {}
    },
    remove(key) {
        try {
            localStorage.removeItem(key);
        } catch {}
    }
};

// Auth API mock
const authApi = {
    async login(email, password) {
        await new Promise(resolve => setTimeout(resolve, 500));
        if (email === 'test@dealxanh.vn' && password === 'password123') {
            return {
                success: true,
                data: {
                    id: 'user1',
                    name: 'Nguyễn Văn A',
                    email: email,
                    phone: '0123456789',
                    avatar: 'https://via.placeholder.com/80',
                    role: 'buyer',
                    token: 'mock-token-123'
                }
            };
        }
        throw new Error('Email hoặc mật khẩu không chính xác');
    },

    async register(data) {
        await new Promise(resolve => setTimeout(resolve, 500));
        return {
            success: true,
            data: {
                id: 'user' + Date.now(),
                name: data.name,
                email: data.email,
                phone: data.phone,
                role: data.role || 'buyer',
                token: 'mock-token-new'
            }
        };
    },

    async verifyOtp(phone, otp) {
        await new Promise(resolve => setTimeout(resolve, 500));
        if (otp === '123456') {
            return {
                success: true,
                data: {
                    id: 'user1',
                    name: 'Nguyễn Văn A',
                    phone: phone,
                    role: 'buyer',
                    token: 'mock-token-123'
                }
            };
        }
        throw new Error('OTP không đúng');
    }
};

class LoginPage {
    constructor() {
        this.currentView = 'email';
        this.selectedRole = 'buyer';
        this.elements = {};
        this.init();
    }

    init() {
        this.cacheElements();
        this.bindEvents();
        this.checkExistingUser();
    }

    cacheElements() {
        this.elements = {
            // Login tabs
            loginTabs: document.querySelectorAll('.login-tab'),
            tabContent: {
                email: document.getElementById('tab-email'),
                phone: document.getElementById('tab-phone')
            },

            // Email login form
            emailForm: document.getElementById('email-form'),
            emailInput: document.getElementById('email'),
            passwordInput: document.getElementById('password'),
            togglePassword: document.getElementById('toggle-password'),
            rememberCheckbox: document.getElementById('remember'),
            forgotLink: document.getElementById('forgot-link'),
            emailSubmit: document.getElementById('email-submit'),

            // Phone login form
            phoneForm: document.getElementById('phone-form'),
            phoneInput: document.getElementById('phone'),
            phoneSubmit: document.getElementById('phone-submit'),

            // OTP form
            otpForm: document.getElementById('otp-form'),
            otpInputs: document.querySelectorAll('.otp-input__digit'),
            resendOtpBtn: document.getElementById('resend-otp'),
            otpSubmit: document.getElementById('otp-submit'),

            // Registration form
            registerForm: document.getElementById('register-form'),
            registerName: document.getElementById('register-name'),
            registerEmail: document.getElementById('register-email'),
            registerPhone: document.getElementById('register-phone'),
            registerPassword: document.getElementById('register-password'),
            registerConfirmPassword: document.getElementById('register-confirm-password'),
            registerSubmit: document.getElementById('register-submit'),

            // Role selector
            roleSelector: document.getElementById('role-selector'),
            roleOptions: document.querySelectorAll('.role-option'),

            // Social buttons
            socialGoogle: document.getElementById('social-google'),
            socialFacebook: document.getElementById('social-facebook'),
            socialZalo: document.getElementById('social-zalo'),

            // Guest banner
            guestButton: document.getElementById('guest-button'),

            // Navigation
            backToEmail: document.getElementById('back-to-email'),
            backToPhone: document.getElementById('back-to-phone'),
            backToLogin: document.getElementById('back-to-login'),

            // Link to register
            toRegister: document.getElementById('to-register'),
            toLogin: document.getElementById('to-login')
        };
    }

    bindEvents() {
        // Tab switching
        this.elements.loginTabs.forEach(tab => {
            tab.addEventListener('click', (e) => this.switchTab(e.target.dataset.tab));
        });

        // Role selection
        this.elements.roleOptions.forEach(option => {
            option.addEventListener('click', () => this.selectRole(option.dataset.role));
        });

        // Email form
        this.elements.emailForm?.addEventListener('submit', (e) => this.handleEmailLogin(e));
        this.elements.togglePassword?.addEventListener('click', () => this.togglePasswordVisibility());
        this.elements.forgotLink?.addEventListener('click', () => this.handleForgotPassword());

        // Phone form
        this.elements.phoneForm?.addEventListener('submit', (e) => this.handlePhoneLogin(e));

        // OTP form
        this.elements.otpInputs?.forEach((input, index) => {
            input.addEventListener('input', (e) => this.handleOtpInput(e, index));
            input.addEventListener('paste', (e) => this.handleOtpPaste(e));
            input.addEventListener('keydown', (e) => this.handleOtpKeydown(e, index));
        });
        this.elements.resendOtpBtn?.addEventListener('click', () => this.resendOtp());
        this.elements.otpForm?.addEventListener('submit', (e) => this.handleOtpVerify(e));

        // Registration form
        this.elements.registerForm?.addEventListener('submit', (e) => this.handleRegister(e));

        // Social login
        this.elements.socialGoogle?.addEventListener('click', () => this.handleSocialLogin('google'));
        this.elements.socialFacebook?.addEventListener('click', () => this.handleSocialLogin('facebook'));
        this.elements.socialZalo?.addEventListener('click', () => this.handleSocialLogin('zalo'));

        // Guest access
        this.elements.guestButton?.addEventListener('click', () => this.handleGuestAccess());

        // Navigation
        this.elements.backToEmail?.addEventListener('click', () => this.showEmailLogin());
        this.elements.backToPhone?.addEventListener('click', () => this.showPhoneLogin());
        this.elements.backToLogin?.addEventListener('click', () => this.showEmailLogin());
        this.elements.toRegister?.addEventListener('click', () => this.showRegister());
        this.elements.toRegisterPhone?.addEventListener('click', () => this.showRegister());
        this.elements.toLogin?.addEventListener('click', () => this.showEmailLogin());

        // Form validation on blur
        this.elements.emailInput?.addEventListener('blur', () => this.validateField('email'));
        this.elements.passwordInput?.addEventListener('blur', () => this.validateField('password'));
        this.elements.phoneInput?.addEventListener('blur', () => this.validateField('phone'));
    }

    switchTab(tab) {
        this.currentView = tab;

        this.elements.loginTabs.forEach(t => {
            t.classList.toggle('login-tab--active', t.dataset.tab === tab);
        });

        Object.entries(this.elements.tabContent).forEach(([key, el]) => {
            if (el) {
                el.style.display = key === tab ? 'block' : 'none';
            }
        });
    }

    selectRole(role) {
        this.selectedRole = role;
        this.elements.roleOptions.forEach(option => {
            option.classList.toggle('role-option--selected', option.dataset.role === role);
        });
    }

    async handleEmailLogin(e) {
        e.preventDefault();

        const email = this.elements.emailInput.value.trim();
        const password = this.elements.passwordInput.value;

        if (!this.validateEmail(email) || !this.validatePassword(password)) {
            return;
        }

        this.setLoading(true, 'email-submit');

        try {
            const response = await authApi.login(email, password);

            if (response.success) {
                storage.set('user', response.data);
                storage.set('token', response.data.token);

                showToast('Đăng nhập thành công!', 'success');

                // Redirect based on role
                setTimeout(() => {
                    const redirect = response.data.role === 'seller' ? '/seller/dashboard' : '/pages/home.html';
                    window.location.href = redirect;
                }, 500);
            }
        } catch (error) {
            showToast(error.message || 'Đăng nhập thất bại', 'error');
            this.elements.emailForm.classList.add('shake');
            setTimeout(() => this.elements.emailForm.classList.remove('shake'), 500);
        } finally {
            this.setLoading(false, 'email-submit');
        }
    }

    async handlePhoneLogin(e) {
        e.preventDefault();

        const phone = this.elements.phoneInput.value.trim();

        if (!this.validatePhone(phone)) {
            return;
        }

        this.setLoading(true, 'phone-submit');

        try {
            // Simulate sending OTP
            await new Promise(resolve => setTimeout(resolve, 1000));

            showToast('Đã gửi mã OTP đến số điện thoại', 'success');
            this.showOtpForm(phone);
        } catch (error) {
            showToast('Không thể gửi OTP. Vui lòng thử lại.', 'error');
        } finally {
            this.setLoading(false, 'phone-submit');
        }
    }

    handleOtpInput(e, index) {
        const input = e.target;
        const value = input.value;

        // Only allow numbers
        input.value = value.replace(/[^0-9]/g, '');

        // Move to next input
        if (value.length === 1 && index < this.elements.otpInputs.length - 1) {
            this.elements.otpInputs[index + 1].focus();
        }

        // Check if all filled
        this.checkOtpComplete();
    }

    handleOtpPaste(e) {
        e.preventDefault();
        const pastedData = e.clipboardData.getData('text').slice(0, 6);

        pastedData.split('').forEach((char, index) => {
            if (this.elements.otpInputs[index] && /[0-9]/.test(char)) {
                this.elements.otpInputs[index].value = char;
            }
        });

        this.checkOtpComplete();
    }

    handleOtpKeydown(e, index) {
        if (e.key === 'Backspace' && !e.target.value && index > 0) {
            this.elements.otpInputs[index - 1].focus();
        }
    }

    checkOtpComplete() {
        const otp = Array.from(this.elements.otpInputs)
            .map(input => input.value)
            .join('');

        if (otp.length === 6) {
            this.elements.otpInputs.forEach(input => input.classList.add('filled'));
        } else {
            this.elements.otpInputs.forEach(input => input.classList.remove('filled'));
        }
    }

    async handleOtpVerify(e) {
        if (e) e.preventDefault();

        const otp = Array.from(this.elements.otpInputs)
            .map(input => input.value)
            .join('');

        if (otp.length !== 6) {
            showToast('Vui lòng nhập đầy đủ mã OTP', 'error');
            return;
        }

        this.setLoading(true, 'otp-submit');

        try {
            const response = await authApi.verifyOtp(this.currentPhone, otp);

            if (response.success) {
                storage.set('user', response.data);
                storage.set('token', response.data.token);

                showToast('Đăng nhập thành công!', 'success');

                setTimeout(() => {
                    const redirect = response.data.role === 'seller' ? '/seller/dashboard' : '/pages/home.html';
                    window.location.href = redirect;
                }, 500);
            }
        } catch (error) {
            showToast(error.message || 'OTP không đúng', 'error');
            this.elements.otpInputs.forEach(input => {
                input.value = '';
                input.classList.remove('filled');
            });
            this.elements.otpInputs[0].focus();
        } finally {
            this.setLoading(false, 'otp-submit');
        }
    }

    resendOtp() {
        const btn = this.elements.resendOtpBtn;
        btn.disabled = true;

        let countdown = 60;
        const timer = setInterval(() => {
            btn.textContent = `Gửi lại (${countdown}s)`;
            countdown--;

            if (countdown < 0) {
                clearInterval(timer);
                btn.disabled = false;
                btn.textContent = 'Gửi lại mã';
            }
        }, 1000);

        showToast('Đã gửi lại mã OTP', 'success');
    }

    async handleRegister(e) {
        e.preventDefault();

        const name = this.elements.registerName.value.trim();
        const email = this.elements.registerEmail.value.trim();
        const phone = this.elements.registerPhone.value.trim();
        const password = this.elements.registerPassword.value;
        const confirmPassword = this.elements.registerConfirmPassword.value;

        if (!this.validateField('name') ||
            !this.validateField('email') ||
            !this.validateField('phone') ||
            !this.validateField('password') ||
            !this.validateField('confirmPassword')) {
            return;
        }

        if (password !== confirmPassword) {
            showToast('Mật khẩu xác nhận không khớp', 'error');
            return;
        }

        this.setLoading(true, 'register-submit');

        try {
            const response = await authApi.register({
                name,
                email,
                phone,
                password,
                role: this.selectedRole
            });

            if (response.success) {
                storage.set('user', response.data);
                storage.set('token', response.data.token);

                showToast('Đăng ký thành công!', 'success');

                setTimeout(() => {
                    const redirect = this.selectedRole === 'seller' ? '/seller/onboarding' : '/pages/home.html';
                    window.location.href = redirect;
                }, 500);
            }
        } catch (error) {
            showToast(error.message || 'Đăng ký thất bại', 'error');
        } finally {
            this.setLoading(false, 'register-submit');
        }
    }

    handleSocialLogin(provider) {
        showToast(`Đăng nhập với ${provider} - Tính năng đang phát triển`, 'info');
    }

    handleGuestAccess() {
        storage.set('guest', true);
        storage.set('guestStartTime', Date.now());
        showToast('Chào mừng! Bạn đang ở chế độ khách', 'success');
        setTimeout(() => window.location.href = '/pages/home.html', 500);
    }

    handleForgotPassword() {
        showToast('Tính năng đặt lại mật khẩu đang phát triển', 'info');
    }

    togglePasswordVisibility() {
        const input = this.elements.passwordInput;
        const icon = this.elements.togglePassword;

        if (input.type === 'password') {
            input.type = 'text';
            icon.innerHTML = `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>`;
        } else {
            input.type = 'password';
            icon.innerHTML = `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>`;
        }
    }

    showEmailLogin() {
        this.switchTab('email');
    }

    showPhoneLogin() {
        this.switchTab('phone');
    }

    showOtpForm(phone) {
        this.currentPhone = phone;
        document.getElementById('phone-view').style.display = 'none';
        document.getElementById('otp-view').style.display = 'block';
        this.elements.otpInputs[0].focus();
    }

    showRegister() {
        document.getElementById('login-view').style.display = 'none';
        document.getElementById('register-view').style.display = 'block';
    }

    showLogin() {
        document.getElementById('register-view').style.display = 'none';
        document.getElementById('login-view').style.display = 'block';
    }

    setLoading(loading, buttonId) {
        const btn = document.getElementById(buttonId);
        if (!btn) return;

        if (loading) {
            btn.disabled = true;
            btn.innerHTML = `
                <div class="login-submit__loading">
                    <div class="login-submit__spinner"></div>
                    <span>Đang xử lý...</span>
                </div>
            `;
        } else {
            btn.disabled = false;
            const originalText = buttonId === 'email-submit' ? 'Đăng nhập' :
                               buttonId === 'phone-submit' ? 'Tiếp tục' :
                               buttonId === 'otp-submit' ? 'Xác nhận' :
                               'Đăng ký';
            btn.innerHTML = originalText;
        }
    }

    validateField(field) {
        let isValid = true;
        let errorElement = null;

        switch (field) {
            case 'email':
                const email = this.elements.emailInput.value.trim();
                isValid = validators.email(email);
                if (!isValid) {
                    showToast('Email không hợp lệ', 'error');
                }
                break;

            case 'password':
                const password = this.elements.passwordInput.value;
                isValid = validators.required(password) && password.length >= 6;
                if (!isValid) {
                    showToast('Mật khẩu phải có ít nhất 6 ký tự', 'error');
                }
                break;

            case 'phone':
                const phone = this.elements.phoneInput.value.trim();
                isValid = validators.phone(phone);
                if (!isValid) {
                    showToast('Số điện thoại không hợp lệ', 'error');
                }
                break;

            case 'name':
                const name = this.elements.registerName.value.trim();
                isValid = validators.required(name);
                if (!isValid) {
                    showToast('Vui lòng nhập họ tên', 'error');
                }
                break;

            case 'confirmPassword':
                const confirmPassword = this.elements.registerConfirmPassword.value;
                const originalPassword = this.elements.registerPassword.value;
                isValid = confirmPassword === originalPassword;
                if (!isValid) {
                    showToast('Mật khẩu xác nhận không khớp', 'error');
                }
                break;
        }

        return isValid;
    }

    validateEmail(email) {
        return validators.email(email);
    }

    validatePassword(password) {
        return validators.required(password) && password.length >= 6;
    }

    validatePhone(phone) {
        return validators.phone(phone);
    }

    checkExistingUser() {
        const user = storage.get('user');
        if (user) {
            // User is already logged in
            showToast('Bạn đã đăng nhập', 'info');
            setTimeout(() => {
                window.location.href = '/pages/home.html';
            }, 1000);
        }
    }
}

// Initialize on DOM ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => new LoginPage());
} else {
    new LoginPage();
}
