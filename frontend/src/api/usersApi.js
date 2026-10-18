import apiClient from '../services/apiClient';

export const usersApi = {
  /**
   * Obtiene el perfil del usuario autenticado.
   */
  async getMe() {
    return await apiClient.get('/users/me');
  },

  /**
   * Actualiza el perfil del usuario autenticado.
   */
  async updateMe(userData) {
    return await apiClient.put('/users/me', userData);
  },

  /**
   * Obtiene la lista de técnicos (solo ADMIN).
   */
  async getTechnicians() {
    return await apiClient.get('/users/technicians');
  },

  /**
   * Da de alta un nuevo técnico (solo ADMIN).
   */
  async createTechnician(techData) {
    return await apiClient.post('/users/technicians', techData);
  },

  /**
   * Actualiza los datos de un técnico (solo ADMIN).
   */
  async updateTechnician(id, techData) {
    return await apiClient.put(`/users/technicians/${id}`, techData);
  },

  /**
   * Desactiva un técnico (solo ADMIN).
   */
  async disableTechnician(id) {
    return await apiClient.patch(`/users/technicians/${id}/disable`);
  },
};

export default usersApi;
