/**
 * Component Loader - DealXanh
 * Load HTML components dynamically for all pages
 */

class ComponentLoader {
    constructor() {
        this.componentsPath = '../components/';
        this.cache = new Map();
    }

    async loadComponent(componentName) {
        // Check cache first
        if (this.cache.has(componentName)) {
            return this.cache.get(componentName);
        }

        try {
            const response = await fetch(this.componentsPath + componentName + '.html');
            if (!response.ok) {
                throw new Error(`Failed to load component: ${componentName}`);
            }
            const html = await response.text();
            this.cache.set(componentName, html);
            return html;
        } catch (error) {
            console.error(`Error loading component ${componentName}:`, error);
            return `<!-- Failed to load ${componentName} -->`;
        }
    }

    async injectComponent(componentName, targetSelector) {
        const target = document.querySelector(targetSelector);
        if (!target) {
            console.error(`Target not found: ${targetSelector}`);
            return false;
        }

        const html = await this.loadComponent(componentName);
        target.innerHTML = html;
        return true;
    }

    async prependComponent(componentName, targetSelector) {
        const target = document.querySelector(targetSelector);
        if (!target) {
            console.error(`Target not found: ${targetSelector}`);
            return false;
        }

        const html = await this.loadComponent(componentName);
        target.insertAdjacentHTML('afterbegin', html);
        return true;
    }

    async appendComponent(componentName, targetSelector) {
        const target = document.querySelector(targetSelector);
        if (!target) {
            console.error(`Target not found: ${targetSelector}`);
            return false;
        }

        const html = await this.loadComponent(componentName);
        target.insertAdjacentHTML('beforeend', html);
        return true;
    }

    // Preload critical components
    async preloadComponents(componentNames) {
        const promises = componentNames.map(name => this.loadComponent(name));
        await Promise.all(promises);
    }
}

// Create singleton instance
const componentLoader = new ComponentLoader();

// Export for use in other scripts
export default componentLoader;
export { ComponentLoader };
