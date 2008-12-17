/**
 * Copyright (c) 2007-2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org•À½epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 * 
 *
 * $Id$
 */
package net.bioclipse.qsar.provider;
import java.util.Collection;
import java.util.List;
import net.bioclipse.qsar.DescriptorType;
import net.bioclipse.qsar.ParameterType;
import net.bioclipse.qsar.QsarFactory;
import net.bioclipse.qsar.QsarPackage;
import net.bioclipse.qsar.descriptor.model.DescriptorParameter;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.emf.edit.provider.ViewerNotification;
/**
 * This is the item provider adapter for a {@link net.bioclipse.qsar.DescriptorType} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class DescriptorTypeItemProvider
        extends ItemProviderAdapter
        implements
                IEditingDomainItemProvider,
                IStructuredItemContentProvider,
                ITreeItemContentProvider,
                IItemLabelProvider,
                IItemPropertySource {
        /**
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        public static final String copyright = "Copyright (c) 2007-2008 The Bioclipse Project and others.\nAll rights reserved. This program and the accompanying materials\nare made available under the terms of the Eclipse Public License v1.0\nwhich accompanies this distribution, and is available at\nwww.eclipse.org\u00ef\u00bf\u03a9epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>\n\nContributors:\n    Ola Spjuth - initial API and implementation\n";
        /**
         * This constructs an instance from a factory and a notifier.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        public DescriptorTypeItemProvider(AdapterFactory adapterFactory) {
                super(adapterFactory);
        }
        /**
         * This returns the property descriptors for the adapted class.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        @Override
        public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
                if (itemPropertyDescriptors == null) {
                        super.getPropertyDescriptors(object);
                        addDescriptorimplPropertyDescriptor(object);
                        addIdPropertyDescriptor(object);
                        addNamespacePropertyDescriptor(object);
                }
                return itemPropertyDescriptors;
        }
        private void addParametersPropertyDescriptor(Object object) {
                ParameterTypeItemProvider p=new ParameterTypeItemProvider(getAdapterFactory());
                DescriptorType dtype=(DescriptorType)object;
                if (dtype.getParameter()!=null){
                        for (ParameterType param : dtype.getParameter()){
                                itemPropertyDescriptors.add
                                (createItemPropertyDescriptor
                                        (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
                                         getResourceLocator(),
                                         param.getKey(),
                                         "description here",
                                         QsarPackage.Literals.PARAMETER_TYPE__VALUE,
                                         true,
                                         false,
                                         false,
                                         null,
                                         "parameters",
                                         null));
                        }
                }
        }
        /**
         * This adds a property descriptor for the Descriptorimpl feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        protected void addDescriptorimplPropertyDescriptor(Object object) {
                itemPropertyDescriptors.add
                        (createItemPropertyDescriptor
                                (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
                                 getResourceLocator(),
                                 getString("_UI_DescriptorType_descriptorimpl_feature"),
                                 getString("_UI_PropertyDescriptor_description", "_UI_DescriptorType_descriptorimpl_feature", "_UI_DescriptorType_type"),
                                 QsarPackage.Literals.DESCRIPTOR_TYPE__DESCRIPTORIMPL,
                                 true,
                                 false,
                                 false,
                                 null,
                                 null,
                                 null));
        }
        /**
         * This adds a property descriptor for the Id feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        protected void addIdPropertyDescriptor(Object object) {
                itemPropertyDescriptors.add
                        (createItemPropertyDescriptor
                                (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
                                 getResourceLocator(),
                                 getString("_UI_DescriptorType_id_feature"),
                                 getString("_UI_PropertyDescriptor_description", "_UI_DescriptorType_id_feature", "_UI_DescriptorType_type"),
                                 QsarPackage.Literals.DESCRIPTOR_TYPE__ID,
                                 true,
                                 false,
                                 false,
                                 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
                                 null,
                                 null));
        }
        /**
         * This adds a property descriptor for the Namespace feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        protected void addNamespacePropertyDescriptor(Object object) {
                itemPropertyDescriptors.add
                        (createItemPropertyDescriptor
                                (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
                                 getResourceLocator(),
                                 getString("_UI_DescriptorType_namespace_feature"),
                                 getString("_UI_PropertyDescriptor_description", "_UI_DescriptorType_namespace_feature", "_UI_DescriptorType_type"),
                                 QsarPackage.Literals.DESCRIPTOR_TYPE__NAMESPACE,
                                 true,
                                 false,
                                 false,
                                 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
                                 null,
                                 null));
        }
        /**
         * This specifies how to implement {@link #getChildren} and is used to deduce an appropriate feature for an
         * {@link org.eclipse.emf.edit.command.AddCommand}, {@link org.eclipse.emf.edit.command.RemoveCommand} or
         * {@link org.eclipse.emf.edit.command.MoveCommand} in {@link #createCommand}.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        @Override
        public Collection<? extends EStructuralFeature> getChildrenFeatures(Object object) {
                if (childrenFeatures == null) {
                        super.getChildrenFeatures(object);
                        childrenFeatures.add(QsarPackage.Literals.DESCRIPTOR_TYPE__PARAMETER);
                }
                return childrenFeatures;
        }
        /**
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        @Override
        protected EStructuralFeature getChildFeature(Object object, Object child) {
                // Check the type of the specified child object and return the proper feature to use for
                // adding (see {@link AddCommand}) it as a child.
                return super.getChildFeature(object, child);
        }
        /**
         * This returns DescriptorType.gif.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        @Override
        public Object getImage(Object object) {
                return overlayImage(object, getResourceLocator().getImage("full/obj16/DescriptorType"));
        }
        /**
         * This returns the label text for the adapted class.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         */
        @Override
        public String getText(Object object) {
                String label = ((DescriptorType)object).getId();
                if (label.indexOf('#')>0){
                        label=label.substring(0, label.lastIndexOf('#'));
                }
                return label == null || label.length() == 0 ?
                        getString("_UI_DescriptorType_type") :
                        getString("_UI_DescriptorType_type") + " " + label;
        }
        /**
         * This handles model notifications by calling {@link #updateChildren} to update any cached
         * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        @Override
        public void notifyChanged(Notification notification) {
                updateChildren(notification);
                switch (notification.getFeatureID(DescriptorType.class)) {
                        case QsarPackage.DESCRIPTOR_TYPE__DESCRIPTORIMPL:
                        case QsarPackage.DESCRIPTOR_TYPE__ID:
                        case QsarPackage.DESCRIPTOR_TYPE__NAMESPACE:
                                fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
                                return;
                        case QsarPackage.DESCRIPTOR_TYPE__PARAMETER:
                                fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
                                return;
                }
                super.notifyChanged(notification);
        }
        /**
         * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s describing the children
         * that can be created under this object.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        @Override
        protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
                super.collectNewChildDescriptors(newChildDescriptors, object);
                newChildDescriptors.add
                        (createChildParameter
                                (QsarPackage.Literals.DESCRIPTOR_TYPE__PARAMETER,
                                 QsarFactory.eINSTANCE.createParameterType()));
        }
        /**
         * Return the resource locator for this item provider's resources.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        @Override
        public ResourceLocator getResourceLocator() {
                return QsarEditPlugin.INSTANCE;
        }
}
