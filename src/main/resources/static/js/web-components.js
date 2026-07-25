/**
 * Web Components - DealXanh
 * Reusable components for all pages using Web Components API
 */

// Skip if Web Components not supported
if (!('customElements' in window) {
    console.warn('Web Components not supported');
}

// ==================== HEADER COMPONENT ====================
class DealHeader extends HTMLElement {
    constructor() {
        super();
    }

    connectedCallback() {
        this.innerHTML = `
            <header class="mobile-header">
                <div class="mobile-header__logo">
                    <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
                    </svg>
                    <span class="mobile-header__brand">DealXanh</span>
                </div>
                <div class="mobile-header__actions">
                    <button class="mobile-header__button" id="search-toggle" aria-label="Search">
                        <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <circle cx="11" cy="11" r="8"></circle>
                            <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                        </svg>
                    </button>
                    <button class="mobile-header__button" id="notification-toggle" aria-label="Notifications">
                        <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
                            <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
                        </svg>
                        <span class="notification-badge">3</span>
                    </button>
                </div>
            </header>
        `;

        // Bind events
        this.bindEvents();
    }

    bindEvents() {
        const searchToggle = this.querySelector('#search-toggle');
        const notifToggle = this.querySelector('#notification-toggle');

        searchToggle?.addEventListener('click', () => {
            this.dispatchEvent(new CustomEvent('search-toggle', { bubbles: true }));
        });

        notifToggle?.addEventListener('click', () => {
            this.dispatchEvent(new CustomEvent('notifications-toggle', { bubbles: true }));
        });
    }
}

// ==================== SEARCH OVERLAY COMPONENT ====================
class DealSearchOverlay extends HTMLElement {
    constructor() {
        super();
    }

    connectedCallback() {
        this.innerHTML = `
            <div class="search-overlay" style="display: none;" aria-hidden="true">
                <div class="search-overlay__container">
                    <div class="search-overlay__input-wrapper">
                        <svg class="search-overlay__icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <circle cx="11" cy="11" r="8"></circle>
                            <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                        </svg>
                        <input type="text" class="search-overlay__input" placeholder="Tìm kiếm món ăn, quán ăn..." aria-label="Search">
                        <button class="search-overlay__close" aria-label="Close search">
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <line x1="18" y1="6" x2="6" y2="18"></line>
                                <line x1="6" y1="6" x2="18" y2="18"></line>
                            </svg>
                        </button>
                    </div>
                    <div class="search-overlay__suggestions">
                        <div class="search-overlay__title">Tìm kiếm phổ biến</div>
                        <div class="search-suggestions">
                            <button class="search-suggestion__chip">Bánh mì</button>
                            <button class="search-suggestion__chip">Cà phê</button>
                            <button class="search-suggestion__chip">Bún phở</button>
                            <button class="search-suggestion__chip">Bánh ngọt</button>
                            <button class="search-suggestion__chip">Trà sữa</button>
                            <button class="search-suggestion__chip">Gà rán</button>
                        </div>
                    </div>
                </div>
            </div>
        `;

        // Bind events
        this.bindEvents();
    }

    bindEvents() {
        const closeBtn = this.querySelector('.search-overlay__close');
        const overlay = this.querySelector('.search-overlay');
        const input = this.querySelector('.search-overlay__input');

        closeBtn?.addEventListener('click', () => this.close());
        overlay?.addEventListener('click', (e) => {
            if (e.target === overlay) this.close();
        });

        // Close on escape
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && this.isVisible()) {
                this.close();
            }
        });

        // Suggestion chips
        this.querySelectorAll('.search-suggestion__chip').forEach(chip => {
            chip.addEventListener('click', () => {
                const term = chip.textContent;
                input.value = term;
                this.dispatchEvent(new CustomEvent('search', {
                    detail: { term },
                    bubbles: true
                }));
                this.close();
            });
        });
    }

    open() {
        const overlay = this.querySelector('.search-overlay');
        if (overlay) {
            overlay.style.display = 'flex';
            overlay.setAttribute('aria-hidden', 'false');
            this.querySelector('.search-overlay__input')?.focus();
        }
    }

    close() {
        const overlay = this.querySelector('.search-overlay');
        if (overlay) {
            overlay.style.display = 'none';
            overlay.setAttribute('aria-hidden', 'true');
        }
    }

    isVisible() {
        return this.querySelector('.search-overlay')?.style.display === 'flex';
    }
}

// ==================== MOBILE NAV COMPONENT ====================
class DealMobileNav extends HTMLElement {
    constructor() {
        super();
    }

    connectedCallback() {
        this.innerHTML = `
            <nav class="mobile-nav">
                <ul class="mobile-nav__list">
                    <li>
                        <a href="home.html" class="mobile-nav__link mobile-nav__link--active">
                            <svg class="mobile-nav__icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                                <polyline points="9 22 9 12 15 12 15 22"></polyline>
                            </svg>
                            <span>Trang chủ</span>
                        </a>
                    </li>
                    <li>
                        <a href="#" class="mobile-nav__link">
                            <svg class="mobile-nav__icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <polygon points="1 6 1 22 8 18 16 22 23 18 23 2 16 6 8 2 1 6"></polygon>
                                <line x1="8" y1="2" x2="8" y2="18"></line>
                                <line x1="16" y1="6" x2="16" y2="22"></line>
                            </svg>
                            <span>Bản đồ</span>
                        </a>
                    </li>
                    <li>
                        <a href="#" class="mobile-nav__link mobile-nav__link--cart">
                            <svg class="mobile-nav__icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <circle cx="9" cy="21" r="1"></circle>
                                <circle cx="20" cy="21" r="1"></circle>
                                <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
                            </svg>
                            <span>Giỏ hàng</span>
                            <span id="mobile-nav-cart-badge" class="mobile-nav__cart__badge">0</span>
                        </a>
                    </li>
                    <li>
                        <a href="#" class="mobile-nav__link">
                            <svg class="mobile-nav__icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                                <polyline points="14 2 14 8 20 8"></polyline>
                                <line x1="16" y1="13" x2="8" y2="13"></line>
                                <line x1="16" y1="17" x2="8" y2="17"></line>
                                <polyline points="10 9 9 9 8 9"></polyline>
                            </svg>
                            <span>Đơn hàng</span>
                        </a>
                    </li>
                    <li>
                        <a href="login.html" class="mobile-nav__link">
                            <svg class="mobile-nav__icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                                <circle cx="12" cy="7" r="4"></circle>
                            </svg>
                            <span>Tài khoản</span>
                        </a>
                    </li>
                </ul>
            </nav>
        `;

        // Highlight active page
        this.highlightActivePage();
    }

    highlightActivePage() {
        const currentPage = window.location.pathname.split('/').pop() || 'home.html';
        this.querySelectorAll('.mobile-nav__link').forEach(link => {
            link.classList.remove('mobile-nav__link--active');
            if (link.getAttribute('href') === currentPage) {
                link.classList.add('mobile-nav__link--active');
            }
        });
    }

    updateCartBadge(count) {
        const badge = this.querySelector('#mobile-nav-cart-badge');
        if (badge) {
            badge.textContent = count > 99 ? '99+' : count;
            badge.style.display = count > 0 ? 'flex' : 'none';
        }
    }
}

// ==================== FOOTER COMPONENT ====================
class DealFooter extends HTMLElement {
    constructor() {
        super();
    }

    connectedCallback() {
        this.innerHTML = `
            <footer class="footer">
                <div class="container">
                    <div class="footer__main">
                        <div class="footer__brand">
                            <div class="footer__logo">
                                <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" stroke-width="2">
                                    <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
                                </svg>
                                <span class="footer__brand-name">DealXanh</span>
                            </div>
                            <p class="footer__description">Nền tảng săn deal đồ ăn cận date giá rẻ. Tiết kiệm đến 70% cho các món ăn ngon từ nhà hàng, quán ăn uy tín.</p>
                            <div class="footer__socials">
                                <a href="#" class="footer__social" aria-label="Facebook">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                                        <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235V6a1 1 0 0 0 1-.5.871V6c0-.1-.006-.2-.018-.3.028V9.43a6.33 6.33 0 0 1 .7-2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.03 12.03 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/>
                                    </svg>
                                </a>
                                <a href="#" class="footer__social" aria-label="Instagram">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                        <rect x="2" y="2" width="20" height="20" rx="5" ry="5"></rect>
                                        <path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z"></path>
                                        <line x1="17.5" y1="6.5" x2="17.51" y2="6.5"></line>
                                    </svg>
                                </a>
                                <a href="#" class="footer__social" aria-label="TikTok">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                                        <path d="M19.59 6.69a4.83 4.83 0 0 1-3.77-4.25V2h-3.45v13.67a2.89 2.89 0 0 1-5.2 1.74 2.89 2.89 0 0 1 2.31-4.64 2.93 2.93 0 0 1 .88.13V9.4a6.84 6.84 0 0 1-1 .05A6.33 6.33 0 0 1 5 20.1a6.34 6.34 0 0 1 .84.1V9.43a6.33 6.33 0 0 0-1 .7-2.81 6.84 6.84 0 0 1 3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/>
                                    </svg>
                                </a>
                            </div>
                        </div>
                        <div class="footer__links">
                            <div class="footer__links-group">
                                <h4 class="footer__links-title">Về DealXanh</h4>
                                <ul class="footer__links-list">
                                    <li><a href="#">Giới thiệu</a></li>
                                    <li><a href="#">Cách hoạt động</a></li>
                                    <li><a href="#">Chính sách</a></li>
                                    <li><a href="#">FAQ</a></li>
                                </ul>
                            </div>
                            <div class="footer__links-group">
                                <h4 class="footer__links-title">Hỗ trợ</h4>
                                <ul class="footer__links-list">
                                    <li><a href="#">Trung tâm trợ giúp</a></li>
                                    <li><a href="#">Liên hệ</a></li>
                                    <li><a href="#">Góp ý</a></li>
                                    <li><a href="#">Báo cáo lỗi</a></li>
                                </ul>
                            </div>
                            <div class="footer__links-group">
                                <h4 class="footer__links-title">Liên hệ</h4>
                                <ul class="footer__links-list">
                                    <li>
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                            <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.05 12.05 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.03 12.03 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path>
                                        </svg>
                                        <span>1900 1234</span>
                                    </li>
                                    <li>
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                            <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                                            <polyline points="22,6 12,13 2,6"></polyline>
                                        </svg>
                                        <span>support@dealxanh.vn</span>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </div>
                    <div class="footer__bottom">
                        <p class="footer__copyright">© 2025 DealXanh. Tất cả quyền được bảo lưu.</p>
                        <div class="footer__legal">
                            <a href="#">Điều khoản sử dụng</a>
                            <a href="#">Chính sách bảo mật</a>
                        </div>
                    </div>
                </div>
            </footer>
        `;
    }
}

// Register components
if ('customElements' in window) {
    customElements.define('deal-header', DealHeader);
    customElements.define('deal-search-overlay', DealSearchOverlay);
    customElements.define('deal-mobile-nav', DealMobileNav);
    customElements.define('deal-footer', DealFooter);
}

// Export for use in other scripts
export { DealHeader, DealSearchOverlay, DealMobileNav, DealFooter };
