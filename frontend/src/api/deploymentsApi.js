import apiClient from '../services/apiClient';

export const deploymentsApi = {
  /**
   * Obtiene todos los despliegues (filtrado según rol por backend).
   */
  async getDeployments() {
    return await apiClient.get('/deployments');
  },

  /**
   * Obtiene exclusivamente los despliegues asignados al técnico actual.
   */
  async getMyDeployments() {
    return await apiClient.get('/deployments/my');
  },

  /**
   * Obtiene los despliegues sin técnico asignado (disponibles para autoasignación).
   */
  async getUnassignedDeployments() {
    return await apiClient.get('/deployments/unassigned');
  },

  /**
   * Obtiene el detalle de un despliegue por su ID.
   */
  async getDeploymentById(id) {
    return await apiClient.get(`/deployments/${id}`);
  },

  /**
   * Reasigna un despliegue a un técnico (solo ADMIN).
   */
  async assignTechnician(deploymentId, technicianId) {
    return await apiClient.put(`/deployments/${deploymentId}/assign-technician/${technicianId}`);
  },

  /**
   * Autoasigna / reclama un despliegue para el técnico autenticado.
   */
  async claimDeployment(deploymentId) {
    return await apiClient.put(`/deployments/${deploymentId}/claim`);
  },

  /**
   * Resuelve o remedia un check de seguridad.
   */
  async resolveCheck(deploymentId, checkId, message) {
    return await apiClient.put(`/deployments/${deploymentId}/checks/${checkId}/resolve`, { message });
  },

  /**
   * Modifica manualmente el estado, compliance o fase de un despliegue.
   */
  async updateDeploymentStatus(deploymentId, data) {
    return await apiClient.put(`/deployments/${deploymentId}/status`, data);
  },

  /**
   * Descarga el reporte PDF del despliegue.
   */
  async downloadReportPdf(deploymentId) {
    await apiClient.downloadBlob(`/deployments/${deploymentId}/report.pdf`, `informe-${deploymentId}.pdf`);
  },

  /**
   * Genera un nuevo análisis de errores con IA.
   */
  async generateAiSummary(deploymentId) {
    return await apiClient.post(`/deployments/${deploymentId}/ai-summary`);
  },

  /**
   * Obtiene el análisis de IA más reciente.
   */
  async getAiSummary(deploymentId) {
    return await apiClient.get(`/deployments/${deploymentId}/ai-summary`);
  },

  /**
   * Obtiene los pasos del despliegue.
   */
  async getSteps(deploymentId) {
    return await apiClient.get(`/deployments/${deploymentId}/steps`);
  },

  /**
   * Obtiene los checks de seguridad del despliegue.
   */
  async getChecks(deploymentId) {
    return await apiClient.get(`/deployments/${deploymentId}/checks`);
  },
};

export default deploymentsApi;
