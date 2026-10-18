import apiClient from '../services/apiClient';

export const auditApi = {
  /**
   * Obtiene todos los eventos de auditoría (solo ADMIN).
   */
  async getAuditLogs() {
    return await apiClient.get('/audit');
  },

  /**
   * Obtiene eventos de auditoría relacionados con un despliegue (solo ADMIN).
   */
  async getAuditLogsByDeployment(deploymentId) {
    return await apiClient.get(`/audit/deployments/${deploymentId}`);
  },

  /**
   * Obtiene eventos de auditoría relacionados con un usuario (solo ADMIN).
   */
  async getAuditLogsByUser(userId) {
    return await apiClient.get(`/audit/users/${userId}`);
  },
};

export default auditApi;
