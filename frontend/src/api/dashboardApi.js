import apiClient from '../services/apiClient';

export const dashboardApi = {
  /**
   * Obtiene las métricas agregadas del dashboard
   * (globales para ADMIN o específicas para TECHNICIAN).
   */
  async getSummary() {
    return await apiClient.get('/dashboard/summary');
  },
};

export default dashboardApi;
