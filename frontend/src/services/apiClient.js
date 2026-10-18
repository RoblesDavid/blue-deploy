const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

/**
 * Cliente HTTP centralizado para interactuar con la API REST de BlueDeploy.
 * Gestiona autenticación mediante Bearer token e intercepta respuestas y errores.
 */
class ApiClient {
  constructor(baseUrl) {
    this.baseUrl = baseUrl;
  }

  getToken() {
    return localStorage.getItem('bluedeploy_token');
  }

  getHeaders(customHeaders = {}) {
    const headers = {
      'Content-Type': 'application/json',
      ...customHeaders,
    };

    const token = this.getToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    return headers;
  }

  async request(endpoint, options = {}) {
    const url = endpoint.startsWith('http') ? endpoint : `${this.baseUrl}${endpoint.startsWith('/') ? '' : '/'}${endpoint}`;
    
    const config = {
      ...options,
      headers: this.getHeaders(options.headers),
    };

    try {
      const response = await fetch(url, config);

      if (response.status === 401) {
        // Token expirado o inválido: limpiar sesión si existía
        if (this.getToken()) {
          localStorage.removeItem('bluedeploy_token');
          localStorage.removeItem('bluedeploy_user');
          window.dispatchEvent(new Event('auth:unauthorized'));
        }
      }

      if (!response.ok) {
        let errorData;
        try {
          errorData = await response.json();
        } catch {
          errorData = { message: `Error HTTP ${response.status}: ${response.statusText}` };
        }
        const error = new Error(errorData.message || errorData.error || 'Error en la petición');
        error.status = response.status;
        error.data = errorData;
        throw error;
      }

      if (response.status === 204) {
        return null;
      }

      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        return await response.json();
      }
      return await response.text();
    } catch (err) {
      throw err;
    }
  }

  get(endpoint, options = {}) {
    return this.request(endpoint, { ...options, method: 'GET' });
  }

  post(endpoint, body, options = {}) {
    return this.request(endpoint, {
      ...options,
      method: 'POST',
      body: body ? JSON.stringify(body) : undefined,
    });
  }

  put(endpoint, body, options = {}) {
    return this.request(endpoint, {
      ...options,
      method: 'PUT',
      body: body ? JSON.stringify(body) : undefined,
    });
  }

  patch(endpoint, body, options = {}) {
    return this.request(endpoint, {
      ...options,
      method: 'PATCH',
      body: body ? JSON.stringify(body) : undefined,
    });
  }

  delete(endpoint, options = {}) {
    return this.request(endpoint, { ...options, method: 'DELETE' });
  }

  async downloadBlob(endpoint, filename = 'report.pdf') {
    const url = endpoint.startsWith('http') ? endpoint : `${this.baseUrl}${endpoint.startsWith('/') ? '' : '/'}${endpoint}`;
    const token = this.getToken();
    const headers = token ? { Authorization: `Bearer ${token}` } : {};

    const response = await fetch(url, { headers });
    if (!response.ok) {
      throw new Error(`Error al descargar archivo: ${response.statusText}`);
    }

    const blob = await response.blob();
    const blobUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = blobUrl;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(blobUrl);
  }
}

export const apiClient = new ApiClient(API_BASE_URL);
export default apiClient;
