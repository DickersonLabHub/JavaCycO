package edu.iastate.javacyco;
/**
 *  This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


/**
PtoolsErrorExceptions are thrown when the PathwayTools server returns the text ":error".  All methods which query a PGDB 
throw a PtoolsErrorException with the message containing the query which caused the error.  Your application must handle these Exceptions.
@author John Van Hemert
*/
@SuppressWarnings("serial")
public class PtoolsErrorException extends Exception {
	
	@SuppressWarnings("unused")
	private int code = 0;

	public PtoolsErrorException() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	

	public PtoolsErrorException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}
	
	public PtoolsErrorException(String message,int code) {
		super(message);
		this.code = code;
		// TODO Auto-generated constructor stub
	}

	public PtoolsErrorException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public PtoolsErrorException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
