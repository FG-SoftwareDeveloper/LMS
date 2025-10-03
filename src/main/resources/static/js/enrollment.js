/**
 * Enrollment Management JavaScript Utilities
 * Provides comprehensive client-side functionality for the enrollment system
 */

class EnrollmentManager {
    constructor() {
        this.apiBase = '/enrollment';
        this.enrollmentCache = new Map();
        this.eventListeners = new Map();
        this.retryAttempts = 3;
        this.retryDelay = 1000;
        
        this.init();
    }
    
    init() {
        this.setupGlobalErrorHandler();
        this.setupOfflineSupport();
        this.setupProgressiveEnhancement();
        this.setupAnalytics();
    }
    
    // ==================== CORE ENROLLMENT FUNCTIONS ====================
    
    /**
     * Enroll a user in a course
     */
    async enrollInCourse(enrollmentData) {
        try {
            this.showLoadingState(true);
            
            const response = await this.apiCall('POST', '/enroll', enrollmentData);
            
            if (response.success) {
                this.trackEvent('enrollment_completed', {
                    courseId: enrollmentData.courseId,
                    source: 'web'
                });
                
                // Handle different enrollment outcomes
                if (response.paymentRequired) {
                    this.redirectToPayment(response.payment.id);
                } else if (response.requiresApproval) {
                    this.showApprovalMessage(response.message);
                } else if (response.waitlisted) {
                    this.showWaitlistMessage(response.waitlistPosition);
                } else {
                    this.showSuccessMessage(response.message);
                    this.redirectToSuccess(response.enrollment.id);
                }
            } else {
                this.showErrorMessage(response.message);
            }
            
        } catch (error) {
            this.handleError('Enrollment failed', error);
        } finally {
            this.showLoadingState(false);
        }
    }
    
    /**
     * Validate prerequisites for a course
     */
    async validatePrerequisites(courseId) {
        try {
            const response = await this.apiCall('GET', `/prerequisites/${courseId}`);
            return response;
        } catch (error) {
            console.error('Error validating prerequisites:', error);
            return { met: false, missing: [], completed: [], canRequestWaiver: false };
        }
    }
    
    /**
     * Check course capacity in real-time
     */
    async checkCapacity(courseId) {
        try {
            const response = await this.apiCall('GET', `/capacity/${courseId}`);
            this.updateCapacityDisplay(response);
            return response;
        } catch (error) {
            console.error('Error checking capacity:', error);
            return null;
        }
    }
    
    /**
     * Validate voucher code
     */
    async validateVoucher(voucherCode, courseId) {
        try {
            const response = await this.apiCall('POST', '/validate-voucher', {
                voucherCode,
                courseId
            });
            
            if (response.valid) {
                this.applyVoucherDiscount(response);
            }
            
            return response;
        } catch (error) {
            console.error('Error validating voucher:', error);
            return { valid: false, message: 'Error validating voucher code' };
        }
    }
    
    /**
     * Withdraw from a course
     */
    async withdrawFromCourse(enrollmentId, reason = '') {
        try {
            const response = await this.apiCall('POST', `/withdraw/${enrollmentId}`, {
                reason
            });
            
            if (response.success) {
                this.trackEvent('enrollment_withdrawn', { enrollmentId });
                this.showSuccessMessage('Successfully withdrawn from course');
                
                // Refresh page or update UI
                setTimeout(() => location.reload(), 2000);
            } else {
                this.showErrorMessage(response.message);
            }
            
            return response;
        } catch (error) {
            this.handleError('Withdrawal failed', error);
            return { success: false };
        }
    }
    
    // ==================== REAL-TIME UPDATES ====================
    
    /**
     * Setup real-time capacity monitoring
     */
    setupCapacityMonitoring(courseId, interval = 30000) {
        const monitor = setInterval(async () => {
            await this.checkCapacity(courseId);
        }, interval);
        
        // Store reference for cleanup
        this.eventListeners.set(`capacity_${courseId}`, monitor);
        
        return monitor;
    }
    
    /**
     * Update capacity display
     */
    updateCapacityDisplay(capacityData) {
        const enrolledElement = document.getElementById('enrolledCount');
        const capacityFill = document.getElementById('capacityFill');
        const enrollBtn = document.getElementById('enrollBtn');
        
        if (enrolledElement) {
            enrolledElement.textContent = capacityData.enrolled;
        }
        
        if (capacityFill && capacityData.capacity > 0) {
            const percentage = (capacityData.enrolled / capacityData.capacity) * 100;
            capacityFill.style.width = percentage + '%';
            
            // Update color based on capacity
            if (percentage >= 90) {
                capacityFill.style.background = '#dc3545'; // Red
            } else if (percentage >= 75) {
                capacityFill.style.background = '#ffc107'; // Yellow
            } else {
                capacityFill.style.background = '#28a745'; // Green
            }
        }
        
        // Update enrollment button if at capacity
        if (enrollBtn && capacityData.isAtCapacity) {
            enrollBtn.innerHTML = '<i class="fas fa-list me-2"></i>Join Waitlist';
            enrollBtn.classList.remove('btn-success');
            enrollBtn.classList.add('btn-warning');
        }
    }
    
    // ==================== PAYMENT INTEGRATION ====================
    
    /**
     * Redirect to payment page
     */
    redirectToPayment(paymentId) {
        window.location.href = `/payment/checkout?paymentId=${paymentId}`;
    }
    
    /**
     * Handle payment completion callback
     */
    handlePaymentComplete(paymentResult) {
        if (paymentResult.success) {
            this.trackEvent('payment_completed', {
                paymentId: paymentResult.paymentId,
                amount: paymentResult.amount
            });
            
            this.showSuccessMessage('Payment completed successfully!');
            this.redirectToSuccess(paymentResult.enrollmentId);
        } else {
            this.showErrorMessage('Payment failed: ' + paymentResult.message);
        }
    }
    
    // ==================== VOUCHER MANAGEMENT ====================
    
    /**
     * Apply voucher discount to UI
     */
    applyVoucherDiscount(voucherData) {
        const originalPrice = parseFloat(document.getElementById('originalPrice')?.textContent.replace('$', '') || '0');
        const finalPrice = originalPrice - voucherData.discount;
        
        // Update price displays
        const discountAmount = document.getElementById('discountAmount');
        const finalPriceElement = document.getElementById('finalPrice');
        const totalPrice = document.getElementById('totalPrice');
        const discountInfo = document.getElementById('discountInfo');
        
        if (discountAmount) discountAmount.textContent = '$' + voucherData.discount.toFixed(2);
        if (finalPriceElement) finalPriceElement.textContent = '$' + finalPrice.toFixed(2);
        if (totalPrice) totalPrice.textContent = '$' + finalPrice.toFixed(2);
        if (discountInfo) discountInfo.style.display = 'block';
        
        // Store voucher data for enrollment
        this.currentVoucher = voucherData;
    }
    
    // ==================== UI FEEDBACK METHODS ====================
    
    showLoadingState(show) {
        const spinner = document.querySelector('.loading-spinner');
        const buttons = document.querySelectorAll('.enrollment-btn, .payment-button');
        
        if (spinner) {
            spinner.style.display = show ? 'inline-block' : 'none';
        }
        
        buttons.forEach(btn => {
            btn.disabled = show;
            if (show) {
                btn.dataset.originalText = btn.textContent;
                btn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Processing...';
            } else if (btn.dataset.originalText) {
                btn.textContent = btn.dataset.originalText;
            }
        });
    }
    
    showSuccessMessage(message) {
        this.showToast(message, 'success');
    }
    
    showErrorMessage(message) {
        this.showToast(message, 'error');
    }
    
    showApprovalMessage(message) {
        this.showToast(message, 'info', 8000);
    }
    
    showWaitlistMessage(position) {
        const message = position 
            ? `You've been added to the waitlist (Position: ${position})`
            : 'You\'ve been added to the waitlist';
        this.showToast(message, 'warning', 8000);
    }
    
    showToast(message, type = 'info', duration = 5000) {
        // Create toast element
        const toast = document.createElement('div');
        toast.className = `alert alert-${type === 'error' ? 'danger' : type} position-fixed`;
        toast.style.cssText = `
            top: 20px;
            right: 20px;
            z-index: 9999;
            min-width: 300px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            border-radius: 8px;
        `;
        
        const icons = {
            success: 'fas fa-check-circle',
            error: 'fas fa-exclamation-triangle',
            warning: 'fas fa-exclamation-circle',
            info: 'fas fa-info-circle'
        };
        
        toast.innerHTML = `
            <i class="${icons[type]} me-2"></i>
            ${message}
            <button type="button" class="btn-close ms-2" onclick="this.parentElement.remove()"></button>
        `;
        
        document.body.appendChild(toast);
        
        // Auto-remove after duration
        setTimeout(() => {
            if (toast.parentElement) {
                toast.remove();
            }
        }, duration);
        
        return toast;
    }
    
    // ==================== API UTILITIES ====================
    
    async apiCall(method, endpoint, data = null) {
        const url = this.apiBase + endpoint;
        const options = {
            method,
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            }
        };
        
        if (data) {
            options.body = JSON.stringify(data);
        }
        
        // Add CSRF token if available
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
        if (csrfToken && csrfHeader) {
            options.headers[csrfHeader] = csrfToken;
        }
        
        let lastError;
        
        // Retry logic
        for (let attempt = 1; attempt <= this.retryAttempts; attempt++) {
            try {
                const response = await fetch(url, options);
                
                if (!response.ok) {
                    throw new Error(`HTTP ${response.status}: ${response.statusText}`);
                }
                
                return await response.json();
                
            } catch (error) {
                lastError = error;
                
                if (attempt < this.retryAttempts) {
                    await this.delay(this.retryDelay * attempt);
                    continue;
                }
                
                throw error;
            }
        }
        
        throw lastError;
    }
    
    delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }
    
    // ==================== ERROR HANDLING ====================
    
    setupGlobalErrorHandler() {
        window.addEventListener('unhandledrejection', (event) => {
            console.error('Unhandled promise rejection:', event.reason);
            this.showErrorMessage('An unexpected error occurred. Please try again.');
            this.trackError('unhandled_rejection', event.reason);
        });
        
        window.addEventListener('error', (event) => {
            console.error('Global error:', event.error);
            this.trackError('global_error', event.error);
        });
    }
    
    handleError(context, error) {
        console.error(`${context}:`, error);
        
        // Show user-friendly message
        let message = 'An error occurred. Please try again.';
        
        if (error.name === 'NetworkError' || error.message.includes('fetch')) {
            message = 'Network error. Please check your connection and try again.';
        } else if (error.message.includes('401')) {
            message = 'Session expired. Please log in again.';
            // Redirect to login after delay
            setTimeout(() => {
                window.location.href = '/login';
            }, 3000);
        } else if (error.message.includes('403')) {
            message = 'You don\'t have permission to perform this action.';
        }
        
        this.showErrorMessage(message);
        this.trackError(context, error);
    }
    
    // ==================== ANALYTICS ====================
    
    setupAnalytics() {
        // Track page views
        this.trackEvent('page_view', {
            page: window.location.pathname,
            timestamp: new Date().toISOString()
        });
        
        // Track user interactions
        document.addEventListener('click', (event) => {
            const target = event.target.closest('[data-track]');
            if (target) {
                this.trackEvent('user_interaction', {
                    action: target.dataset.track,
                    element: target.tagName.toLowerCase(),
                    text: target.textContent.trim().substring(0, 50)
                });
            }
        });
    }
    
    trackEvent(eventName, properties = {}) {
        // Send to analytics service
        if (typeof gtag !== 'undefined') {
            gtag('event', eventName, properties);
        }
        
        // Send to internal analytics
        this.sendAnalytics('event', {
            name: eventName,
            properties,
            timestamp: new Date().toISOString(),
            sessionId: this.getSessionId()
        }).catch(error => {
            console.warn('Analytics tracking failed:', error);
        });
    }
    
    trackError(context, error) {
        const errorData = {
            context,
            message: error.message,
            stack: error.stack,
            userAgent: navigator.userAgent,
            url: window.location.href,
            timestamp: new Date().toISOString()
        };
        
        this.sendAnalytics('error', errorData).catch(console.warn);
    }
    
    async sendAnalytics(type, data) {
        try {
            await fetch('/analytics/track', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ type, data })
            });
        } catch (error) {
            // Fail silently for analytics
        }
    }
    
    getSessionId() {
        let sessionId = sessionStorage.getItem('enrollmentSessionId');
        if (!sessionId) {
            sessionId = 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
            sessionStorage.setItem('enrollmentSessionId', sessionId);
        }
        return sessionId;
    }
    
    // ==================== OFFLINE SUPPORT ====================
    
    setupOfflineSupport() {
        // Store enrollment data locally for offline scenarios
        window.addEventListener('online', () => {
            this.syncOfflineData();
            this.showToast('Connection restored', 'success');
        });
        
        window.addEventListener('offline', () => {
            this.showToast('You are offline. Data will be saved locally.', 'warning', 8000);
        });
    }
    
    async syncOfflineData() {
        const offlineData = this.getOfflineData();
        
        for (const item of offlineData) {
            try {
                await this.apiCall(item.method, item.endpoint, item.data);
                this.removeOfflineData(item.id);
            } catch (error) {
                console.error('Failed to sync offline data:', error);
            }
        }
    }
    
    saveOfflineData(method, endpoint, data) {
        const offlineData = this.getOfflineData();
        const item = {
            id: Date.now() + Math.random(),
            method,
            endpoint,
            data,
            timestamp: new Date().toISOString()
        };
        
        offlineData.push(item);
        localStorage.setItem('enrollmentOfflineData', JSON.stringify(offlineData));
    }
    
    getOfflineData() {
        const data = localStorage.getItem('enrollmentOfflineData');
        return data ? JSON.parse(data) : [];
    }
    
    removeOfflineData(id) {
        const offlineData = this.getOfflineData().filter(item => item.id !== id);
        localStorage.setItem('enrollmentOfflineData', JSON.stringify(offlineData));
    }
    
    // ==================== PROGRESSIVE ENHANCEMENT ====================
    
    setupProgressiveEnhancement() {
        // Enhance forms with AJAX
        this.enhanceForms();
        
        // Add keyboard navigation
        this.setupKeyboardNavigation();
        
        // Add mobile optimizations
        this.setupMobileOptimizations();
        
        // Setup accessibility features
        this.setupAccessibility();
    }
    
    enhanceForms() {
        document.querySelectorAll('form[data-ajax]').forEach(form => {
            form.addEventListener('submit', async (event) => {
                event.preventDefault();
                
                const formData = new FormData(form);
                const data = Object.fromEntries(formData.entries());
                
                try {
                    const response = await this.apiCall('POST', form.action, data);
                    
                    if (response.success) {
                        this.showSuccessMessage(response.message);
                    } else {
                        this.showErrorMessage(response.message);
                    }
                } catch (error) {
                    this.handleError('Form submission failed', error);
                }
            });
        });
    }
    
    setupKeyboardNavigation() {
        document.addEventListener('keydown', (event) => {
            // Escape key closes modals
            if (event.key === 'Escape') {
                const openModal = document.querySelector('.modal.show');
                if (openModal) {
                    bootstrap.Modal.getInstance(openModal)?.hide();
                }
            }
            
            // Enter key on buttons
            if (event.key === 'Enter' && event.target.matches('button[data-action]')) {
                event.target.click();
            }
        });
    }
    
    setupMobileOptimizations() {
        if (/Android|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)) {
            // Add mobile-specific classes
            document.body.classList.add('mobile-device');
            
            // Optimize touch interactions
            let touchStartY = 0;
            document.addEventListener('touchstart', (e) => {
                touchStartY = e.touches[0].clientY;
            });
            
            document.addEventListener('touchend', (e) => {
                const touchEndY = e.changedTouches[0].clientY;
                const deltaY = touchStartY - touchEndY;
                
                // Pull to refresh
                if (deltaY < -100 && window.scrollY === 0) {
                    location.reload();
                }
            });
        }
    }
    
    setupAccessibility() {
        // Add ARIA labels to dynamic content
        document.querySelectorAll('[data-loading]').forEach(element => {
            element.setAttribute('aria-live', 'polite');
        });
        
        // Announce important changes to screen readers
        this.announceToScreenReader = (message) => {
            const announcer = document.createElement('div');
            announcer.setAttribute('aria-live', 'assertive');
            announcer.setAttribute('aria-atomic', 'true');
            announcer.className = 'sr-only';
            announcer.textContent = message;
            
            document.body.appendChild(announcer);
            setTimeout(() => announcer.remove(), 1000);
        };
    }
    
    // ==================== NAVIGATION HELPERS ====================
    
    redirectToSuccess(enrollmentId) {
        setTimeout(() => {
            window.location.href = `/enrollment/success?enrollmentId=${enrollmentId}`;
        }, 2000);
    }
    
    // ==================== CLEANUP ====================
    
    destroy() {
        // Clear all intervals and timeouts
        this.eventListeners.forEach((listener, key) => {
            if (typeof listener === 'number') {
                clearInterval(listener);
            }
        });
        this.eventListeners.clear();
        
        // Clear cache
        this.enrollmentCache.clear();
        
        // Remove event listeners
        // (In a real implementation, you'd store references to remove them)
    }
}

// ==================== GLOBAL UTILITY FUNCTIONS ====================

/**
 * Format currency values
 */
function formatCurrency(amount, currency = 'USD') {
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: currency
    }).format(amount);
}

/**
 * Format dates for display
 */
function formatDate(date, options = {}) {
    const defaultOptions = {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    };
    
    return new Intl.DateTimeFormat('en-US', { ...defaultOptions, ...options })
        .format(new Date(date));
}

/**
 * Debounce function for search inputs
 */
function debounce(func, wait) {
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

/**
 * Check if user is online
 */
function isOnline() {
    return navigator.onLine;
}

/**
 * Generate unique token
 */
function generateToken() {
    return Date.now().toString(36) + Math.random().toString(36).substr(2);
}

// ==================== INITIALIZATION ====================

// Initialize enrollment manager when DOM is ready
let enrollmentManager;

document.addEventListener('DOMContentLoaded', () => {
    enrollmentManager = new EnrollmentManager();
    
    // Make it globally accessible
    window.enrollmentManager = enrollmentManager;
});

// Export for module systems
if (typeof module !== 'undefined' && module.exports) {
    module.exports = EnrollmentManager;
}