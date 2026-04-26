/**
 * Utility Functions - DealXanh
 */

// Format currency (VND)
export function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
    }).format(amount);
}

// Format percentage
export function formatPercentage(value) {
    return `${Math.round(value)}%`;
}

// Format date/time
export function formatDateTime(date, options = {}) {
    const defaultOptions = {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    };
    return new Intl.DateTimeFormat('vi-VN', { ...defaultOptions, ...options }).format(new Date(date));
}

// Format time only
export function formatTime(date) {
    return new Intl.DateTimeFormat('vi-VN', {
        hour: '2-digit',
        minute: '2-digit'
    }).format(new Date(date));
}

// Format relative time (e.g., "2h ago", "30min left")
export function formatRelativeTime(date) {
    const now = new Date();
    const target = new Date(date);
    const diff = target - now;
    const absDiff = Math.abs(diff);

    const minutes = Math.floor(absDiff / 60000);
    const hours = Math.floor(absDiff / 3600000);
    const days = Math.floor(absDiff / 86400000);

    if (absDiff < 60000) {
        return diff > 0 ? 'Vài giây tới' : 'Vừa xong';
    } else if (minutes < 60) {
        return diff > 0 ? `${minutes} phút nữa` : `${minutes} phút trước`;
    } else if (hours < 24) {
        return diff > 0 ? `${hours} giờ nữa` : `${hours} giờ trước`;
    } else {
        return diff > 0 ? `${days} ngày nữa` : `${days} ngày trước`;
    }
}

// Format distance
export function formatDistance(meters) {
    if (meters < 1000) {
        return `${Math.round(meters)}m`;
    }
    return `${(meters / 1000).toFixed(1)}km`;
}

// Calculate discount percentage
export function calculateDiscount(originalPrice, discountedPrice) {
    return ((originalPrice - discountedPrice) / originalPrice) * 100;
}

// Check if deal is expiring soon
export function isExpiringSoon(expiryDate, thresholdMinutes = 60) {
    const now = new Date();
    const expiry = new Date(expiryDate);
    const diff = expiry - now;
    const minutes = diff / 60000;
    return minutes > 0 && minutes <= thresholdMinutes;
}

// Check if deal is expired
export function isExpired(expiryDate) {
    return new Date(expiryDate) < new Date();
}

// Get urgency level for expiry
export function getUrgencyLevel(expiryDate) {
    const now = new Date();
    const expiry = new Date(expiryDate);
    const diff = expiry - now;
    const hours = diff / 3600000;

    if (diff <= 0) return 'expired';
    if (hours < 1) return 'danger';
    if (hours < 4) return 'warning';
    return 'safe';
}

// Debounce function
export function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Throttle function
export function throttle(func, limit) {
    let inThrottle;
    return function executedFunction(...args) {
        if (!inThrottle) {
            func(...args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
}

// Copy to clipboard
export async function copyToClipboard(text) {
    try {
        await navigator.clipboard.writeText(text);
        return true;
    } catch (err) {
        console.error('Failed to copy:', err);
        return false;
    }
}

// Generate unique ID
export function generateId() {
    return Date.now().toString(36) + Math.random().toString(36).substr(2);
}

// Local storage helpers
export const storage = {
    get(key, defaultValue = null) {
        try {
            const item = localStorage.getItem(key);
            return item ? JSON.parse(item) : defaultValue;
        } catch (error) {
            console.error('Error reading from localStorage:', error);
            return defaultValue;
        }
    },

    set(key, value) {
        try {
            localStorage.setItem(key, JSON.stringify(value));
        } catch (error) {
            console.error('Error writing to localStorage:', error);
        }
    },

    remove(key) {
        try {
            localStorage.removeItem(key);
        } catch (error) {
            console.error('Error removing from localStorage:', error);
        }
    },

    clear() {
        try {
            localStorage.clear();
        } catch (error) {
            console.error('Error clearing localStorage:', error);
        }
    }
};

// Session storage helpers
export const sessionStorage = {
    get(key, defaultValue = null) {
        try {
            const item = window.sessionStorage.getItem(key);
            return item ? JSON.parse(item) : defaultValue;
        } catch (error) {
            console.error('Error reading from sessionStorage:', error);
            return defaultValue;
        }
    },

    set(key, value) {
        try {
            window.sessionStorage.setItem(key, JSON.stringify(value));
        } catch (error) {
            console.error('Error writing to sessionStorage:', error);
        }
    },

    remove(key) {
        try {
            window.sessionStorage.removeItem(key);
        } catch (error) {
            console.error('Error removing from sessionStorage:', error);
        }
    }
};

// URL helpers
export const url = {
    getParams() {
        const params = new URLSearchParams(window.location.search);
        const result = {};
        params.forEach((value, key) => {
            result[key] = value;
        });
        return result;
    },

    setParams(params) {
        const url = new URL(window.location);
        Object.entries(params).forEach(([key, value]) => {
            url.searchParams.set(key, value);
        });
        window.history.pushState({}, '', url);
    },

    removeParam(key) {
        const url = new URL(window.location);
        url.searchParams.delete(key);
        window.history.pushState({}, '', url);
    }
};

// Validation helpers
export const validators = {
    email(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    },

    phone(phone) {
        const re = /(84|0[3|5|7|8|9])+([0-9]{8})\b/;
        return re.test(phone);
    },

    required(value) {
        return value !== null && value !== undefined && value.toString().trim() !== '';
    },

    minLength(value, min) {
        return value && value.length >= min;
    },

    maxLength(value, max) {
        return value && value.length <= max;
    }
};

// Toast notification
export function showToast(message, type = 'success', duration = 3000) {
    const toast = document.createElement('div');
    toast.className = `toast toast--${type}`;
    toast.innerHTML = `
        <div style="display: flex; align-items: center; gap: 12px;">
            ${type === 'success' ? '<svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/></svg>' : ''}
            ${type === 'error' ? '<svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/></svg>' : ''}
            ${type === 'warning' ? '<svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/></svg>' : ''}
            <span style="font-size: 14px; font-weight: 500;">${message}</span>
        </div>
    `;
    document.body.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(20px)';
        toast.style.transition = 'all 0.3s ease-out';
        setTimeout(() => toast.remove(), 300);
    }, duration);
}

// Modal helpers
export function openModal(content) {
    const overlay = document.createElement('div');
    overlay.className = 'modal-overlay';
    overlay.innerHTML = content;
    document.body.appendChild(overlay);

    // Close on overlay click
    overlay.addEventListener('click', (e) => {
        if (e.target === overlay) {
            closeModal(overlay);
        }
    });

    // Close on escape key
    const handleEscape = (e) => {
        if (e.key === 'Escape') {
            closeModal(overlay);
        }
    };
    document.addEventListener('keydown', handleEscape);

    return {
        element: overlay,
        close: () => {
            closeModal(overlay);
            document.removeEventListener('keydown', handleEscape);
        }
    };
}

function closeModal(overlay) {
    overlay.style.opacity = '0';
    overlay.style.transition = 'opacity 0.2s ease-out';
    setTimeout(() => overlay.remove(), 200);
}

// Loading spinner
export function showLoading(container) {
    const spinner = document.createElement('div');
    spinner.className = 'spinner';
    container.appendChild(spinner);
    return spinner;
}

// Get user location
export async function getUserLocation() {
    return new Promise((resolve, reject) => {
        if (!navigator.geolocation) {
            reject(new Error('Geolocation is not supported'));
            return;
        }

        navigator.geolocation.getCurrentPosition(
            (position) => {
                resolve({
                    latitude: position.coords.latitude,
                    longitude: position.coords.longitude
                });
            },
            (error) => {
                reject(error);
            },
            {
                enableHighAccuracy: true,
                timeout: 10000,
                maximumAge: 300000
            }
        );
    });
}

// Calculate distance between two coordinates (in meters)
export function calculateDistance(lat1, lon1, lat2, lon2) {
    const R = 6371000; // Earth's radius in meters
    const dLat = toRad(lat2 - lat1);
    const dLon = toRad(lon2 - lon1);

    const a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
        Math.sin(dLon / 2) * Math.sin(dLon / 2);

    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
}

function toRad(degrees) {
    return degrees * (Math.PI / 180);
}

// Share functionality
export async function shareContent(title, text, url) {
    if (navigator.share) {
        try {
            await navigator.share({ title, text, url });
            return true;
        } catch (err) {
            if (err.name !== 'AbortError') {
                console.error('Error sharing:', err);
            }
            return false;
        }
    }
    return false;
}

// Lazy load images
export function lazyLoadImages() {
    const images = document.querySelectorAll('img[data-src]');

    const imageObserver = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const img = entry.target;
                img.src = img.dataset.src;
                img.removeAttribute('data-src');
                observer.unobserve(img);
            }
        });
    });

    images.forEach(img => imageObserver.observe(img));
}

// Countdown timer
export function createCountdown(endDate, callback) {
    let interval;

    function update() {
        const now = new Date().getTime();
        const distance = new Date(endDate).getTime() - now;

        if (distance < 0) {
            clearInterval(interval);
            callback({ expired: true });
            return;
        }

        const days = Math.floor(distance / (1000 * 60 * 60 * 24));
        const hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
        const seconds = Math.floor((distance % (1000 * 60)) / 1000);

        callback({ expired: false, days, hours, minutes, seconds });
    }

    update();
    interval = setInterval(update, 1000);

    return () => clearInterval(interval);
}

// Format countdown time for display
export function formatCountdown({ days, hours, minutes, seconds }) {
    const parts = [];

    if (days > 0) parts.push(`${days}d`);
    if (hours > 0) parts.push(`${hours}h`);
    if (minutes > 0) parts.push(`${minutes}m`);
    if (seconds > 0 || parts.length === 0) parts.push(`${seconds}s`);

    return parts.join(' ');
}
