// S3 Browser Application
class S3Browser {
    constructor() {        this.baseUrl = '/api';
        this.token = localStorage.getItem('authToken');
        this.currentPath = '';
        this.uploadQueue = [];
        
        this.init();
    }
    
    init() {
        this.setupEventListeners();
        this.checkAuth();
    }
    
    setupEventListeners() {
        // Login form
        document.getElementById('loginForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.login();
        });
        
        // Config form
        document.getElementById('configForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.saveConfiguration();
        });
        
        // File upload
        const fileInput = document.getElementById('fileInput');
        fileInput.addEventListener('change', (e) => {
            this.handleFileSelect(e.target.files);
        });
        
        // Drag and drop
        const uploadArea = document.getElementById('uploadArea');
        uploadArea.addEventListener('dragover', (e) => {
            e.preventDefault();
            uploadArea.classList.add('dragover');
        });
        
        uploadArea.addEventListener('dragleave', (e) => {
            e.preventDefault();
            uploadArea.classList.remove('dragover');
        });
        
        uploadArea.addEventListener('drop', (e) => {
            e.preventDefault();
            uploadArea.classList.remove('dragover');
            this.handleFileSelect(e.dataTransfer.files);
        });
        
        // Search
        document.getElementById('searchInput').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                this.search();
            }
        });
    }
    
    checkAuth() {
        if (!this.token) {
            this.showLogin();
        } else {
            this.validateToken();
        }
    }
    
    showLogin() {
        document.getElementById('mainApp').classList.add('d-none');
        const loginModal = new bootstrap.Modal(document.getElementById('loginModal'));
        loginModal.show();
    }
    
    async login() {
        const form = document.getElementById('loginForm');
        const formData = new FormData(form);
        const credentials = {
            username: formData.get('username'),
            password: formData.get('password')
        };
        
        try {
            const response = await fetch(`${this.baseUrl}/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(credentials)
            });
            
            if (response.ok) {
                const data = await response.json();
                this.token = data.accessToken;
                localStorage.setItem('authToken', this.token);
                
                document.getElementById('usernameDisplay').textContent = credentials.username;
                bootstrap.Modal.getInstance(document.getElementById('loginModal')).hide();
                document.getElementById('mainApp').classList.remove('d-none');
                
                this.loadFiles();
                this.loadConfiguration();
            } else {
                this.showError('loginError', 'Invalid credentials');
            }
        } catch (error) {
            this.showError('loginError', 'Connection error: ' + error.message);
        }
    }
    
    async validateToken() {
        try {
            const response = await fetch(`${this.baseUrl}/auth/validate`, {
                headers: {
                    'Authorization': `Bearer ${this.token}`
                }
            });
            
            if (response.ok) {
                document.getElementById('mainApp').classList.remove('d-none');
                this.loadFiles();
                this.loadConfiguration();
            } else {
                this.showLogin();
            }
        } catch (error) {
            this.showLogin();
        }
    }
    
    logout() {
        localStorage.removeItem('authToken');
        this.token = null;
        this.showLogin();
    }
    
    async loadFiles(prefix = '') {
        this.showLoading(true);
        
        try {
            const url = new URL(`${window.location.origin}${this.baseUrl}/s3/objects`);
            if (prefix) {
                url.searchParams.append('prefix', prefix);
            }
            
            const response = await fetch(url, {
                headers: {
                    'Authorization': `Bearer ${this.token}`
                }
            });
            
            if (response.ok) {
                const files = await response.json();
                this.renderFiles(files);
                this.updateBreadcrumb(prefix);
                this.currentPath = prefix;
            } else {
                this.showError('filesError', 'Failed to load files');
            }
        } catch (error) {
            this.showError('filesError', 'Connection error: ' + error.message);
        } finally {
            this.showLoading(false);
        }
    }
    
    renderFiles(files) {
        const tbody = document.getElementById('filesTableBody');
        const emptyState = document.getElementById('emptyState');
        
        if (files.length === 0) {
            tbody.innerHTML = '';
            emptyState.classList.remove('d-none');
            return;
        }
        
        emptyState.classList.add('d-none');
        
        // Sort files: directories first, then by name
        files.sort((a, b) => {
            if (a.directory !== b.directory) {
                return b.directory - a.directory;
            }
            return a.key.localeCompare(b.key);
        });
        
        tbody.innerHTML = files.map(file => {
            const icon = file.directory ? 
                '<i class="bi bi-folder-fill folder-icon"></i>' : 
                this.getFileIcon(file.key);
            
            const size = file.directory ? '-' : this.formatFileSize(file.size);
            const modified = file.lastModified ? 
                new Date(file.lastModified).toLocaleString() : '-';
            
            const actions = file.directory ? 
                `<button class="btn btn-sm btn-outline-primary" onclick="app.openFolder('${file.key}')">
                    <i class="bi bi-folder-open"></i> Open
                </button>` :
                `<div class="btn-group" role="group">
                    <button class="btn btn-sm btn-outline-success" onclick="app.downloadFile('${file.key}')">
                        <i class="bi bi-download"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="app.deleteFile('${file.key}')">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>`;
            
            return `
                <tr>
                    <td>
                        <span class="file-icon">${icon}</span>
                        ${this.getFileName(file.key)}
                    </td>
                    <td class="file-size">${size}</td>
                    <td>${modified}</td>
                    <td>${actions}</td>
                </tr>
            `;
        }).join('');
    }
    
    getFileIcon(filename) {
        const ext = filename.split('.').pop().toLowerCase();
        const iconMap = {
            // Images
            'jpg': 'bi-image file-type-image',
            'jpeg': 'bi-image file-type-image',
            'png': 'bi-image file-type-image',
            'gif': 'bi-image file-type-image',
            'svg': 'bi-image file-type-image',
            'webp': 'bi-image file-type-image',
            
            // Documents
            'pdf': 'bi-file-earmark-pdf file-type-document',
            'doc': 'bi-file-earmark-word file-type-document',
            'docx': 'bi-file-earmark-word file-type-document',
            'xls': 'bi-file-earmark-excel file-type-document',
            'xlsx': 'bi-file-earmark-excel file-type-document',
            'ppt': 'bi-file-earmark-ppt file-type-document',
            'pptx': 'bi-file-earmark-ppt file-type-document',
            'txt': 'bi-file-earmark-text file-type-document',
            
            // Archives
            'zip': 'bi-file-earmark-zip file-type-archive',
            'rar': 'bi-file-earmark-zip file-type-archive',
            '7z': 'bi-file-earmark-zip file-type-archive',
            'tar': 'bi-file-earmark-zip file-type-archive',
            'gz': 'bi-file-earmark-zip file-type-archive',
            
            // Video
            'mp4': 'bi-file-earmark-play file-type-video',
            'avi': 'bi-file-earmark-play file-type-video',
            'mov': 'bi-file-earmark-play file-type-video',
            'wmv': 'bi-file-earmark-play file-type-video',
            'mkv': 'bi-file-earmark-play file-type-video',
            
            // Audio
            'mp3': 'bi-file-earmark-music file-type-audio',
            'wav': 'bi-file-earmark-music file-type-audio',
            'flac': 'bi-file-earmark-music file-type-audio',
            'aac': 'bi-file-earmark-music file-type-audio',
            
            // Code
            'js': 'bi-file-earmark-code file-type-code',
            'html': 'bi-file-earmark-code file-type-code',
            'css': 'bi-file-earmark-code file-type-code',
            'java': 'bi-file-earmark-code file-type-code',
            'py': 'bi-file-earmark-code file-type-code',
            'cpp': 'bi-file-earmark-code file-type-code',
            'c': 'bi-file-earmark-code file-type-code',
            'json': 'bi-file-earmark-code file-type-code',
            'xml': 'bi-file-earmark-code file-type-code',
            'yaml': 'bi-file-earmark-code file-type-code',
            'yml': 'bi-file-earmark-code file-type-code'
        };
        
        const iconClass = iconMap[ext] || 'bi-file-earmark';
        return `<i class="bi ${iconClass}"></i>`;
    }
    
    getFileName(key) {
        if (key.endsWith('/')) {
            const parts = key.slice(0, -1).split('/');
            return parts[parts.length - 1] + '/';
        }
        const parts = key.split('/');
        return parts[parts.length - 1];
    }
    
    formatFileSize(bytes) {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }
    
    updateBreadcrumb(path) {
        const breadcrumb = document.getElementById('breadcrumb');
        const upButton = document.getElementById('upButton');
        
        if (!path) {
            breadcrumb.innerHTML = `
                <li class="breadcrumb-item active" aria-current="page">
                    <i class="bi bi-house"></i> Root
                </li>
            `;
            upButton.disabled = true;
            return;
        }
        
        upButton.disabled = false;
        const parts = path.split('/').filter(part => part);
        let currentPath = '';
        
        let html = `
            <li class="breadcrumb-item">
                <a href="#" onclick="app.loadFiles('')">
                    <i class="bi bi-house"></i> Root
                </a>
            </li>
        `;
        
        parts.forEach((part, index) => {
            currentPath += part + '/';
            if (index === parts.length - 1) {
                html += `
                    <li class="breadcrumb-item active" aria-current="page">
                        ${part}
                    </li>
                `;
            } else {
                html += `
                    <li class="breadcrumb-item">
                        <a href="#" onclick="app.loadFiles('${currentPath}')">${part}</a>
                    </li>
                `;
            }
        });
        
        breadcrumb.innerHTML = html;
    }
    
    openFolder(key) {
        this.loadFiles(key);
    }
    
    goUp() {
        if (!this.currentPath) return;
        
        const parts = this.currentPath.split('/').filter(part => part);
        parts.pop(); // Remove last part
        const newPath = parts.length > 0 ? parts.join('/') + '/' : '';
        this.loadFiles(newPath);
    }
    
    selectFiles() {
        document.getElementById('fileInput').click();
    }
    
    handleFileSelect(files) {
        Array.from(files).forEach(file => {
            this.uploadFile(file);
        });
    }
    
    async uploadFile(file) {
        const key = this.currentPath + file.name;
        const progressContainer = this.createProgressIndicator(file.name);
        
        try {
            const formData = new FormData();
            formData.append('file', file);
            
            const response = await fetch(`${this.baseUrl}/s3/objects/${encodeURIComponent(key)}`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${this.token}`
                },
                body: formData
            });
            
            if (response.ok) {
                this.updateProgress(progressContainer, 100, 'Upload complete');
                setTimeout(() => {
                    progressContainer.remove();
                    this.refresh();
                }, 2000);
            } else {
                this.updateProgress(progressContainer, 0, 'Upload failed', true);
            }
        } catch (error) {
            this.updateProgress(progressContainer, 0, 'Upload error: ' + error.message, true);
        }
    }
    
    createProgressIndicator(filename) {
        let container = document.querySelector('.progress-container');
        if (!container) {
            container = document.createElement('div');
            container.className = 'progress-container';
            document.body.appendChild(container);
        }
        
        const progressDiv = document.createElement('div');
        progressDiv.className = 'upload-progress';
        progressDiv.innerHTML = `
            <div class="file-name">${filename}</div>
            <div class="progress">
                <div class="progress-bar" role="progressbar" style="width: 0%"></div>
            </div>
            <div class="status-text">Uploading...</div>
        `;
        
        container.appendChild(progressDiv);
        return progressDiv;
    }
    
    updateProgress(container, percent, status, isError = false) {
        const progressBar = container.querySelector('.progress-bar');
        const statusText = container.querySelector('.status-text');
        
        progressBar.style.width = percent + '%';
        progressBar.className = `progress-bar ${isError ? 'bg-danger' : 'bg-success'}`;
        statusText.textContent = status;
        statusText.className = `status-text ${isError ? 'text-danger' : ''}`;
    }
    
    async downloadFile(key) {
        try {
            const response = await fetch(`${this.baseUrl}/s3/objects/${encodeURIComponent(key)}/download`, {
                headers: {
                    'Authorization': `Bearer ${this.token}`
                }
            });
            
            if (response.ok) {
                const blob = await response.blob();
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = this.getFileName(key);
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
                window.URL.revokeObjectURL(url);
            } else {
                this.showError('filesError', 'Failed to download file');
            }
        } catch (error) {
            this.showError('filesError', 'Download error: ' + error.message);
        }
    }
    
    async deleteFile(key) {
        if (!confirm(`Are you sure you want to delete "${this.getFileName(key)}"?`)) {
            return;
        }
        
        try {
            const response = await fetch(`${this.baseUrl}/s3/objects/${encodeURIComponent(key)}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${this.token}`
                }
            });
            
            if (response.ok) {
                this.refresh();
            } else {
                this.showError('filesError', 'Failed to delete file');
            }
        } catch (error) {
            this.showError('filesError', 'Delete error: ' + error.message);
        }
    }
    
    async loadConfiguration() {
        try {
            const response = await fetch(`${this.baseUrl}/config`, {
                headers: {
                    'Authorization': `Bearer ${this.token}`
                }
            });
            
            if (response.ok) {
                const config = await response.json();
                this.populateConfigForm(config);
            }
        } catch (error) {
            console.error('Failed to load configuration:', error);
        }
    }
    
    populateConfigForm(config) {
        document.getElementById('configEndpoint').value = config.endpoint || '';
        document.getElementById('configAccessKey').value = config.accessKey || '';
        document.getElementById('configSecretKey').value = '';
        document.getElementById('configBucket').value = config.bucket || '';
        document.getElementById('configRegion').value = config.region || '';
        document.getElementById('configPathStyle').checked = config.pathStyleAccess || false;
    }
    
    async testConfiguration() {
        const form = document.getElementById('configForm');
        const formData = new FormData(form);
        const config = {
            endpoint: formData.get('endpoint'),
            accessKey: formData.get('accessKey'),
            secretKey: formData.get('secretKey'),
            bucket: formData.get('bucket'),
            region: formData.get('region'),
            pathStyleAccess: formData.has('pathStyleAccess')
        };
        
        try {
            const response = await fetch(`${this.baseUrl}/config/test`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.token}`
                },
                body: JSON.stringify(config)
            });
            
            if (response.ok) {
                this.showSuccess('configSuccess', 'Configuration test successful');
            } else {
                const error = await response.text();
                this.showError('configError', 'Configuration test failed: ' + error);
            }
        } catch (error) {
            this.showError('configError', 'Connection error: ' + error.message);
        }
    }
    
    async saveConfiguration() {
        const form = document.getElementById('configForm');
        const formData = new FormData(form);
        const config = {
            endpoint: formData.get('endpoint'),
            accessKey: formData.get('accessKey'),
            secretKey: formData.get('secretKey'),
            bucket: formData.get('bucket'),
            region: formData.get('region'),
            pathStyleAccess: formData.has('pathStyleAccess')
        };
        
        try {
            const response = await fetch(`${this.baseUrl}/config`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.token}`
                },
                body: JSON.stringify(config)
            });
            
            if (response.ok) {
                this.showSuccess('configSuccess', 'Configuration saved successfully');
                setTimeout(() => {
                    bootstrap.Modal.getInstance(document.getElementById('configModal')).hide();
                    this.refresh();
                }, 1500);
            } else {
                const error = await response.text();
                this.showError('configError', 'Failed to save configuration: ' + error);
            }
        } catch (error) {
            this.showError('configError', 'Connection error: ' + error.message);
        }
    }
    
    search() {
        const query = document.getElementById('searchInput').value.trim();
        if (query) {
            this.loadFiles(query);
        } else {
            this.refresh();
        }
    }
    
    refresh() {
        this.loadFiles(this.currentPath);
    }
    
    showLoading(show) {
        const spinner = document.getElementById('loadingSpinner');
        const table = document.getElementById('filesTable');
        
        if (show) {
            spinner.classList.remove('d-none');
            table.classList.add('d-none');
        } else {
            spinner.classList.add('d-none');
            table.classList.remove('d-none');
        }
    }
    
    showError(elementId, message) {
        const element = document.getElementById(elementId);
        if (element) {
            element.textContent = message;
            element.classList.remove('d-none');
            setTimeout(() => {
                element.classList.add('d-none');
            }, 5000);
        }
    }
    
    showSuccess(elementId, message) {
        const element = document.getElementById(elementId);
        if (element) {
            element.textContent = message;
            element.classList.remove('d-none');
            setTimeout(() => {
                element.classList.add('d-none');
            }, 3000);
        }
    }
}

// Global functions
function logout() {
    app.logout();
}

function goUp() {
    app.goUp();
}

function refresh() {
    app.refresh();
}

function search() {
    app.search();
}

function selectFiles() {
    app.selectFiles();
}

function testConfiguration() {
    app.testConfiguration();
}

// Initialize application
const app = new S3Browser();